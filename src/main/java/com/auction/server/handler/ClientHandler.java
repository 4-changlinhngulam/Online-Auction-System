package com.auction.server.handler;

import com.auction.server.service.AuctionManager;
import com.auction.server.service.AuctionService;
import com.auction.server.service.BidService;
import com.auction.server.service.ItemService;
import com.auction.server.service.UserService;
import com.auction.shared.model.entity.Auction;
import com.auction.shared.model.entity.BidObserver;
import com.auction.shared.model.entity.Item;
import com.auction.shared.model.entity.User;
import com.auction.shared.protocol.Request;
import com.auction.shared.protocol.Response;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable, BidObserver {

    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());

    private final Socket socket;
    private final UserService userService;
    private final ItemService itemService;
    private final BidService bidService;
    private final AuctionService auctionService;

    // - Concurrency: Dùng `LinkedBlockingQueue` theo mô hình Producer-Consumer để
    // gửi thông báo bất đồng bộ.
    // - Lý do: Nếu gửi (writeObject) trực tiếp ngay khi có sự kiện, luồng xử lý
    // mạng có thể bị block do độ trễ IO,
    // khiến các client khác bị kẹt. Hàng đợi giúp tách biệt việc nhận/xử lý và việc
    // gửi đi.
    private final BlockingQueue<Response> messageQueue = new LinkedBlockingQueue<>();
    private Thread senderThread;

    private User currentUser = null;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.userService = new UserService();
        this.itemService = new ItemService();
        this.bidService = new BidService(AuctionManager.getInstance());
        this.auctionService = new AuctionService();
    }

    @Override
    public void run() {
        try (Socket ignored = this.socket;
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.flush();

            // Khởi chạy luồng chuyên dụng để gửi dữ liệu (Sender Thread)
            senderThread = new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        Response res = messageQueue.take(); // Chờ đến khi có message
                        out.writeObject(res);
                        out.flush();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.log(Level.INFO, "Sender thread bị ngắt.", e);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Lỗi khi gửi dữ liệu qua Socket. Tự động ngắt Observer.", e);
                    AuctionManager.getInstance().removeObserver(this);
                }
            });
            senderThread.start();

            // Luồng chính liên tục đọc Request (Receiver)
            while (true) {
                Request req = (Request) in.readObject();
                Response res = dispatch(req);
                messageQueue.offer(res); // Đẩy response vào hàng đợi thay vì ghi trực tiếp
            }

        } catch (EOFException eof) {
            LOGGER.log(Level.INFO, "Kết nối client đã đóng: " + socket.getRemoteSocketAddress());
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Lỗi xử lý client: " + e.getMessage(), e);
        } finally {
            if (senderThread != null) {
                senderThread.interrupt(); // Dừng luồng gửi
            }
            // Hủy đăng ký Observer để tránh Memory Leak
            AuctionManager.getInstance().removeObserver(this);
            LOGGER.log(Level.INFO, "Đã dọn dẹp và hủy theo dõi cho Client: " + socket.getRemoteSocketAddress());
        }
    }

    private Response dispatch(Request req) {
        if (req == null || req.getType() == null) {
            return Response.error("Request không hợp lệ");
        }

        return switch (req.getType()) {
            case LOGIN -> processLogin(req);
            case REGISTER -> processRegister(req);
            case LOGOUT -> processLogout();

            case GET_USER_PROFILE -> processGetUserProfile(req);
            case UPDATE_USER_PROFILE -> processUpdateUserProfile(req);

            case CREATE_ITEM -> processCreateItem(req);
            case GET_ALL_ITEMS -> itemService.getAllItems();
            case GET_ITEM -> processGetItem(req);
            case UPDATE_ITEM -> processUpdateItem(req);
            case DELETE_ITEM -> processDeleteItem(req);
            case SEARCH_ITEM -> processSearchItem(req);

            case CREATE_AUCTION -> processCreateAuction(req);
            case GET_AUCTION -> processGetAuction(req);
            case GET_ALL_AUCTIONS -> auctionService.getAllAuctions();
            case CLOSE_AUCTION -> processCloseAuction(req);

            case PLACE_BID -> processPlaceBid(req);
            case GET_BID_HISTORY -> processGetBidHistory(req);

            case BAN_USER -> processBanUser(req);
            case GET_ALL_USERS -> userService.getAllUsers();
            case SUBSCRIBE_AUCTION -> processSubscribeAuction(req);

            default -> Response.error("RequestType không được hỗ trợ: " + req.getType());
        };
    }

    // --- CÁC HÀM XỬ LÝ USER SERVICE ---

    private Response processLogin(Request req) {
        try {
            String[] credentials = (String[]) req.getPayload();
            Response res = userService.login(credentials[0], credentials[1]);
            if (res.isSuccess() && res.getData() instanceof User) {
                this.currentUser = (User) res.getData();
            }
            return res;
        } catch (Exception e) {
            return Response.error("Sai định dạng dữ liệu đăng nhập: " + e.getMessage());
        }
    }

    private Response processRegister(Request req) {
        try {
            User user = (User) req.getPayload();
            return userService.register(user);
        } catch (Exception e) {
            return Response.error("Sai định dạng dữ liệu đăng ký: " + e.getMessage());
        }
    }

    private Response processGetUserProfile(Request req) {
        try {
            String userId = (String) req.getPayload();
            return userService.getUserById(userId);
        } catch (Exception e) {
            return Response.error("ID người dùng không hợp lệ: " + e.getMessage());
        }
    }

    // --- CÁC HÀM XỬ LÝ ITEM VÀ BID SERVICE ---

    private Response processCreateItem(Request req) {
        if (currentUser == null)
            return Response.error("Vui lòng đăng nhập.");
        try {
            Item item = (Item) req.getPayload();
            return itemService.createItem(item);
        } catch (Exception e) {
            return Response.error("Dữ liệu sản phẩm không hợp lệ: " + e.getMessage());
        }
    }

    private Response processGetItem(Request req) {
        try {
            String itemId = (String) req.getPayload();
            return itemService.getItem(itemId);
        } catch (Exception e) {
            return Response.error("ID sản phẩm không hợp lệ: " + e.getMessage());
        }
    }

    private Response processUpdateItem(Request req) {
        try {
            Item item = (Item) req.getPayload();
            return itemService.updateItem(item);
        } catch (Exception e) {
            return Response.error("Dữ liệu cập nhật không hợp lệ: " + e.getMessage());
        }
    }

    private Response processDeleteItem(Request req) {
        try {
            String itemId = (String) req.getPayload();
            return itemService.deleteItem(itemId);
        } catch (Exception e) {
            return Response.error("ID sản phẩm xóa không hợp lệ: " + e.getMessage());
        }
    }

    private Response processSearchItem(Request req) {
        try {
            String keyword = (String) req.getPayload();
            return itemService.searchItemsByName(keyword);
        } catch (Exception e) {
            return Response.error("Từ khóa tìm kiếm không hợp lệ: " + e.getMessage());
        }
    }

    private Response processPlaceBid(Request req) {
        if (currentUser == null)
            return Response.error("Vui lòng đăng nhập.");
        try {
            Object[] data = (Object[]) req.getPayload();
            String auctionId = (String) data[0];
            String bidderId = currentUser.getId(); // Sử dụng Session ID để tránh giả mạo
            double amount = (double) data[2];
            return bidService.placeBid(auctionId, bidderId, amount);
        } catch (Exception e) {
            return Response.error("Dữ liệu đặt giá không hợp lệ: " + e.getMessage());
        }
    }

    private Response processGetBidHistory(Request req) {
        try {
            String auctionId = (String) req.getPayload();
            return bidService.getBidHistory(auctionId);
        } catch (Exception e) {
            return Response.error("ID phiên đấu giá không hợp lệ: " + e.getMessage());
        }
    }

    // --- CÁC HÀM XỬ LÝ MỚI THÊM ---

    private Response processLogout() {
        this.currentUser = null;
        return new Response(true, "Đăng xuất thành công", null);
    }

    private Response processUpdateUserProfile(Request req) {
        if (currentUser == null)
            return Response.error("Vui lòng đăng nhập.");
        try {
            User user = (User) req.getPayload();
            if (!user.getId().equals(currentUser.getId())) {
                return Response.error("Không có quyền cập nhật thông tin người khác.");
            }
            return userService.updateProfile(user);
        } catch (Exception e) {
            return Response.error("Dữ liệu cập nhật user không hợp lệ: " + e.getMessage());
        }
    }

    private Response processCreateAuction(Request req) {
        if (currentUser == null)
            return Response.error("Vui lòng đăng nhập.");
        try {
            Auction auction = (Auction) req.getPayload();
            return auctionService.createAuction(auction);
        } catch (Exception e) {
            return Response.error("Dữ liệu tạo phiên đấu giá không hợp lệ: " + e.getMessage());
        }
    }

    private Response processGetAuction(Request req) {
        try {
            String auctionId = (String) req.getPayload();
            return auctionService.getAuction(auctionId);
        } catch (Exception e) {
            return Response.error("ID phiên đấu giá không hợp lệ: " + e.getMessage());
        }
    }

    private Response processCloseAuction(Request req) {
        try {
            String auctionId = (String) req.getPayload();
            return auctionService.closeAuction(auctionId);
        } catch (Exception e) {
            return Response.error("ID phiên đấu giá không hợp lệ: " + e.getMessage());
        }
    }

    private Response processBanUser(Request req) {
        try {
            String userId = (String) req.getPayload();
            return userService.banUser(userId);
        } catch (Exception e) {
            return Response.error("ID người dùng không hợp lệ: " + e.getMessage());
        }
    }

    private Response processSubscribeAuction(Request req) {
        AuctionManager.getInstance().addObserver(this);
        return new Response(true, "Đăng ký nhận thông báo đấu giá thành công", null);
    }

    // --- IMPLEMENTATION CHO BID OBSERVER ---

    @Override
    public void update(Item item, double newPrice, String lastBidderId) {
        Object[] updateData = new Object[] { item, newPrice, lastBidderId };
        Response notification = new Response(true, "NOTIFICATION_NEW_BID", updateData);

        // Đẩy thông báo vào hàng đợi (non-blocking)
        boolean accepted = messageQueue.offer(notification);
        if (!accepted) {
            LOGGER.log(Level.WARNING, "Hàng đợi gửi tin của Client bị đầy, không thể gửi thông báo mới.");
        }
    }
}
