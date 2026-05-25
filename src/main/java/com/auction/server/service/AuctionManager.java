package com.auction.server.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.UserDAO;
import com.auction.shared.exception.DataPersistenceException;
import com.auction.shared.exception.EntityNotFoundException;
import com.auction.shared.model.entity.Auction;
import com.auction.shared.model.entity.BidObserver;
import com.auction.shared.model.entity.BidTransaction;
import com.auction.shared.model.entity.Bidder;
import com.auction.shared.model.entity.User;
import com.auction.shared.model.enums.AuctionStatus;
import com.auction.shared.protocol.Response;


/** Singleton – Quản lý toàn bộ phiên đấu giá. Xử lý concurrency + Observer notify. */
public class AuctionManager {
    // 1. Singleton: Đảm bảo chỉ có 1 Manager duy nhất trên toàn Server
    private static volatile AuctionManager instance;

    // 2. Lưu trữ các phiên đấu giá đang chạy.
    private final Map<String, Auction> activeAuctions;

    // 3. Danh sách các Client đang theo dõi đấu giá (Dùng cho Observer Pattern)
    private final List<BidObserver> observers;

    // 4. Kết nối với Database
    private final AuctionDAO auctionDAO;

    // Khởi tạo một bộ đếm giờ với luồng chạy ngầm (pool size = 5)
    private final ScheduledExecutorService scheduler;
    private AuctionManager() {
        this.activeAuctions = new ConcurrentHashMap<>();
        this.observers = new ArrayList<>();
        this.auctionDAO = new AuctionDAO();
        this.scheduler = Executors.newScheduledThreadPool(5);
    }
    // Phương thức public static để lấy thể hiện duy nhất (Double-checked locking)
    public static AuctionManager getInstance() {
        AuctionManager auctionManager = instance; // lấy dữ liệu instance vào biến cục bộ để tối ưu bộ nhớ
        if (auctionManager == null) {
            synchronized (AuctionManager.class) {
                auctionManager = instance;
                if (auctionManager == null) {
                    instance = auctionManager = new AuctionManager();
                }
            }
        }
        return auctionManager;
    }
    public void addAuction(Auction auction) {
        activeAuctions.put(auction.getId(), auction);
    }

    /**
     * [Fix 2] Nạp lại các phiên đấu giá đang mở từ Database khi Server khởi động
     */
    public void init() {
        System.out.println("Đang khôi phục các phiên đấu giá từ Database...");
        List<Auction> openAuctions = auctionDAO.getOpenAuctions();
        for (Auction auction : openAuctions) {
            addAuction(auction);
            scheduleAuctionEnd(auction);
            System.out.println("- Đã khôi phục và tiếp tục đếm giờ cho phiên: " + auction.getId());
        }
    }

    public void endAuction(String auctionId) {
        Auction auction = activeAuctions.get(auctionId);
        if (auction != null) {
            auction.setStatus(AuctionStatus.FINISHED);
            try {
                auctionDAO.update(auction);
                notifyObservers(auction);
                System.out.println("Phiên đấu giá " + auctionId + " đã kết thúc!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    // OBSERVER PATTERN (CƠ CHẾ THÔNG BÁO PUSH)

    public void addObserver(BidObserver observer) {
        if (observer != null) {
            synchronized (observers) {
                if (!observers.contains(observer)) {
                    observers.add(observer);
                }
            }
        }
    }

    public void removeObserver(BidObserver observer) {
        if (observer != null) {
            synchronized (observers) {
                observers.remove(observer);
            }
        }
    }

    // Hàm này sẽ lặp qua tất cả các Client đang kết nối và báo cho họ biết có giá mới
    private void notifyObservers(Auction updatedAuction) {
        String winnerId = (updatedAuction.getCurrentWinner() != null) 
                          ? updatedAuction.getCurrentWinner().getId() : "";

        List<BidObserver> copyList;
        synchronized (observers) {
            // Tạo bản sao để tránh ConcurrentModificationException khi duyệt
            copyList = new ArrayList<>(observers);
        }

        for (BidObserver observer : copyList) {
            try {
                observer.update(updatedAuction.getItem(), updatedAuction.getCurrentPrice(), winnerId);
            } catch (Exception e) {
                System.err.println("Lỗi gửi thông báo cho 1 client, gỡ bỏ client: " + e.getMessage());
                removeObserver(observer);
            }
        }
    }

    /**
     * Lên lịch tự động kết thúc phiên đấu giá
     */
    public void scheduleAuctionEnd(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = auction.getEndTime();

        // Tính khoảng thời gian từ bây giờ đến lúc kết thúc
        long delayInMillis = Duration.between(now, endTime).toMillis();

        if (delayInMillis <= 0) {
            // Nếu thời gian kết thúc ở trong quá khứ, đóng phiên ngay lập tức
            endAuction(auction.getId());
        } else {
            // Đặt báo thức: Lệnh () -> endAuction(...) sẽ được chạy sau "delayInMillis"
            scheduler.schedule(() -> {
                System.out.println("Hệ thống tự động chốt phiên đấu giá: " + auction.getId());
                endAuction(auction.getId());
            }, delayInMillis, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Xử lý một lượt đặt giá mới (Real-time).
     * BẮT BUỘC dùng 'synchronized' để chặn nhiều người đặt giá cùng lúc.
     */
    public Response processNewBid(String auctionId, String bidderId, double bidAmount) {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            return new Response(false, "Phiên đấu giá không tồn tại.", null);
        }

        // Đồng bộ hóa trên chính đối tượng phiên đấu giá này để tránh Race Condition đặt giá
        synchronized (auction) {
            Bidder bidder;
            try {
                UserDAO userDAO = new UserDAO();
                User user = userDAO.findById(bidderId);
                if (!(user instanceof Bidder)) {
                    return new Response(false, "Tài khoản không có quyền đặt giá.", null);
                }
                bidder = (Bidder) user;
            } catch (EntityNotFoundException | DataPersistenceException e) {
                return new Response(false, "Lỗi xác thực người dùng: " + e.getMessage(), null);
            }

            // 1. ỦY QUYỀN cho Auction tự xử lý logic kiểm tra giá (Bypass đồng bộ hóa ở Entity)
            boolean isValidBid = auction.handleNewBid(bidder, bidAmount);

            if (!isValidBid) {
                return new Response(false, "Mức giá không hợp lệ hoặc phiên đã kết thúc.", null);
            }

            // 2. NẾU HỢP LỆ -> GHI DATABASE
            try {
                BidTransaction transaction = new BidTransaction();
                transaction.setAuctionId(auctionId);
                transaction.setBidderId(bidderId);
                transaction.setAmount(bidAmount);

                BidTransactionDAO bidDao = new BidTransactionDAO();
                bidDao.save(transaction); // Lưu lịch sử

                auctionDAO.update(auction); // Lưu giá mới của phiên

                // 3. THÔNG BÁO CHO CÁC CLIENT KHÁC
                notifyObservers(auction);

                System.out.println("User " + bidderId + " đặt giá thành công: $" + bidAmount);
                return new Response(true, "Đặt giá thành công!", null);

            } catch (DataPersistenceException | EntityNotFoundException e) {
                System.err.println("Lỗi Database khi lưu Bid: " + e.getMessage());
                // Hoàn tác RAM đơn giản cho bài tập lớn (revert giá trị)
                List<BidTransaction> history = auction.getBidHistory();
                if (history != null && !history.isEmpty()) {
                    history.remove(history.size() - 1);
                    if (!history.isEmpty()) {
                        BidTransaction prev = history.get(history.size() - 1);
                        auction.setCurrentPrice(prev.getAmount());
                        Bidder prevWinner = new Bidder();
                        prevWinner.setId(prev.getBidderId());
                        auction.setCurrentWinner(prevWinner);
                    } else {
                        auction.setCurrentPrice(auction.getItem().getStartingPrice());
                        auction.setCurrentWinner(null);
                    }
                }
                return new Response(false, "Lỗi máy chủ khi xử lý đặt giá.", null);
            }
        }
    }
}

