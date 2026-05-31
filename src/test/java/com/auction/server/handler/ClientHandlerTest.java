package com.auction.server.handler;

import com.auction.server.dao.DatabaseConnection;
import com.auction.server.dao.UserDAO;
import com.auction.shared.model.entity.Bidder;
import com.auction.shared.model.enums.UserStatus;
import com.auction.shared.protocol.Request;
import com.auction.shared.protocol.RequestType;
import com.auction.shared.protocol.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class ClientHandlerTest {

    private ServerSocket serverSocket;
    private int port;
    private Thread serverThread;
    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws Exception {
        userDAO = new UserDAO();

        // Khởi tạo cấu trúc bảng Database H2
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE, " +
                    "password VARCHAR(255), " +
                    "email VARCHAR(100), " +
                    "role VARCHAR(20), " +
                    "status VARCHAR(20))");
            stmt.execute("TRUNCATE TABLE users");
        }

        // Khởi động server socket trên một cổng ngẫu nhiên
        serverSocket = new ServerSocket(0);
        port = serverSocket.getLocalPort();

        serverThread = new Thread(() -> {
            try {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                handler.run();
            } catch (Exception e) {
                // Bỏ qua lỗi đóng socket khi dừng test
            }
        });
        serverThread.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (serverSocket != null) {
            serverSocket.close();
        }
        if (serverThread != null) {
            serverThread.interrupt();
        }
    }

    @Test
    @DisplayName("Test kết nối Socket và gửi yêu cầu đăng nhập qua ClientHandler")
    void testClientHandler_Login() throws Exception {
        // Chuẩn bị tài khoản người dùng trong DB với mật khẩu mã hóa BCrypt
        Bidder bidder = new Bidder("socket_user", BCrypt.hashpw("password123", BCrypt.gensalt()), "socket@gmail.com");
        bidder.setId("USR_SOCKET_01");
        bidder.setStatus(UserStatus.ACTIVE);
        userDAO.save(bidder);

        // Thiết lập kết nối client
        try (Socket clientSocket = new Socket("localhost", port);
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

            out.flush();

            // Gửi yêu cầu đăng nhập
            Request req = new Request(RequestType.LOGIN, new String[]{"socket_user", "password123"});
            out.writeObject(req);
            out.flush();

            // Đọc phản hồi từ Server
            Response res = (Response) in.readObject();

            assertNotNull(res);
            assertTrue(res.isSuccess());
            assertEquals("Đăng nhập thành công.", res.getMessage());
        }
    }

    @Test
    @DisplayName("Test ClientHandler: Đăng ký tài khoản mới qua socket")
    void testClientHandler_Register() throws Exception {
        try (Socket clientSocket = new Socket("localhost", port);
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

            out.flush();

            Bidder bidder = new Bidder("reg_socket_user", "password123", "reg_socket@gmail.com");
            bidder.setId("USR_SOCKET_REG");
            bidder.setRole(com.auction.shared.model.enums.UserRole.BIDDER);

            Request req = new Request(RequestType.REGISTER, bidder);
            out.writeObject(req);
            out.flush();

            Response res = (Response) in.readObject();
            assertNotNull(res);
            assertTrue(res.isSuccess());
            assertEquals("Đăng ký tài khoản thành công.", res.getMessage());
        }
    }

    @Test
    @DisplayName("Test ClientHandler: Đặt giá khi chưa đăng nhập (Trả về lỗi)")
    void testClientHandler_PlaceBid_Unauthenticated() throws Exception {
        try (Socket clientSocket = new Socket("localhost", port);
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

            out.flush();

            Request req = new Request(RequestType.PLACE_BID, new Object[]{"AUC_ID", "BIDDER_ID", 100.0});
            out.writeObject(req);
            out.flush();

            Response res = (Response) in.readObject();
            assertNotNull(res);
            assertFalse(res.isSuccess());
            assertEquals("Vui lòng đăng nhập.", res.getMessage());
        }
    }

    @Test
    @DisplayName("Test ClientHandler: Request không hợp lệ")
    void testClientHandler_InvalidRequest() throws Exception {
        try (Socket clientSocket = new Socket("localhost", port);
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

            out.flush();

            Request req = new Request(null, null);
            out.writeObject(req);
            out.flush();

            Response res = (Response) in.readObject();
            assertNotNull(res);
            assertFalse(res.isSuccess());
            assertEquals("Request không hợp lệ", res.getMessage());
        }
    }

    @Test
    @DisplayName("Test chuỗi hành động đã xác thực qua ClientHandler")
    void testClientHandler_AuthenticatedSequence() throws Exception {
        try (Socket clientSocket = new Socket("localhost", port);
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

            out.flush();

            // 1. Đăng ký tài khoản
            Bidder bidder = new Bidder("seq_user", "pass123", "seq@gmail.com");
            bidder.setId("USR_SEQ_01");
            bidder.setRole(com.auction.shared.model.enums.UserRole.BIDDER);
            Request regReq = new Request(RequestType.REGISTER, bidder);
            out.writeObject(regReq);
            out.flush();
            Response regRes = (Response) in.readObject();
            assertTrue(regRes.isSuccess());

            // 2. Đăng nhập
            Request loginReq = new Request(RequestType.LOGIN, new String[]{"seq_user", "pass123"});
            out.writeObject(loginReq);
            out.flush();
            Response loginRes = (Response) in.readObject();
            assertTrue(loginRes.isSuccess());

            // 3. Xem Profile
            Request profileReq = new Request(RequestType.GET_USER_PROFILE, "USR_SEQ_01");
            out.writeObject(profileReq);
            out.flush();
            Response profileRes = (Response) in.readObject();
            assertTrue(profileRes.isSuccess());

            // 4. Cập nhật Profile
            bidder.setEmail("updated_seq@gmail.com");
            Request updateProfileReq = new Request(RequestType.UPDATE_USER_PROFILE, bidder);
            out.writeObject(updateProfileReq);
            out.flush();
            Response updateProfileRes = (Response) in.readObject();
            assertTrue(updateProfileRes.isSuccess());

            // 5. Thử tạo Item (Thất bại vì là BIDDER)
            com.auction.shared.model.entity.Electronics item = new com.auction.shared.model.entity.Electronics("ITEM_SEQ_01", "Laptop Test", 1000.0);
            Request createItemReq = new Request(RequestType.CREATE_ITEM, item);
            out.writeObject(createItemReq);
            out.flush();
            Response createItemRes = (Response) in.readObject();
            assertFalse(createItemRes.isSuccess()); // Trả về lỗi vì BIDDER không được phép tạo sản phẩm

            // 6. Lấy danh sách Items
            Request getItemsReq = new Request(RequestType.GET_ALL_ITEMS, null);
            out.writeObject(getItemsReq);
            out.flush();
            Response getItemsRes = (Response) in.readObject();
            assertTrue(getItemsRes.isSuccess());

            // 7. Tìm kiếm Item
            Request searchReq = new Request(RequestType.SEARCH_ITEM, "Laptop");
            out.writeObject(searchReq);
            out.flush();
            Response searchRes = (Response) in.readObject();
            assertTrue(searchRes.isSuccess());

            // 8. Đăng ký nhận thông báo đấu giá (SUBSCRIBE_AUCTION)
            Request subReq = new Request(RequestType.SUBSCRIBE_AUCTION, "AUC_SEQ_01");
            out.writeObject(subReq);
            out.flush();
            Response subRes = (Response) in.readObject();
            assertTrue(subRes.isSuccess());

            // 9. Cài đặt Auto-Bid (Thất bại vì phiên đấu giá không tồn tại)
            Request autoBidReq = new Request(RequestType.SETUP_AUTO_BID, new Object[]{"AUC_SEQ_01", "USR_SEQ_01", 2000.0});
            out.writeObject(autoBidReq);
            out.flush();
            Response autoBidRes = (Response) in.readObject();
            assertFalse(autoBidRes.isSuccess());

            // 10. Hủy Auto-Bid (Thất bại vì phiên đấu giá không tồn tại)
            Request cancelAutoBidReq = new Request(RequestType.CANCEL_AUTO_BID, "AUC_SEQ_01");
            out.writeObject(cancelAutoBidReq);
            out.flush();
            Response cancelAutoBidRes = (Response) in.readObject();
            assertFalse(cancelAutoBidRes.isSuccess());

            // 11. Đăng xuất
            Request logoutReq = new Request(RequestType.LOGOUT, null);
            out.writeObject(logoutReq);
            out.flush();
            Response logoutRes = (Response) in.readObject();
            assertTrue(logoutRes.isSuccess());
        }
    }

    @Test
    @DisplayName("Test các hành động của Admin và Seller qua ClientHandler")
    void testClientHandler_AdminAndSellerActions() throws Exception {
        // 1. Tạo tài khoản Admin và Seller trong DB
        com.auction.shared.model.entity.Admin admin = new com.auction.shared.model.entity.Admin("admin_socket", BCrypt.hashpw("admin123", BCrypt.gensalt()), "admin@gmail.com");
        admin.setId("USR_SOCKET_ADMIN");
        admin.setStatus(UserStatus.ACTIVE);
        userDAO.save(admin);

        com.auction.shared.model.entity.Seller seller = new com.auction.shared.model.entity.Seller("seller_socket", BCrypt.hashpw("seller123", BCrypt.gensalt()), "seller@gmail.com");
        seller.setId("USR_SOCKET_SELLER");
        seller.setStatus(UserStatus.ACTIVE);
        userDAO.save(seller);

        // Chạy kết nối của Seller trước
        try (Socket clientSocket = new Socket("localhost", port);
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

            out.flush();

            // Seller đăng nhập
            Request loginReq = new Request(RequestType.LOGIN, new String[]{"seller_socket", "seller123"});
            out.writeObject(loginReq);
            out.flush();
            Response loginRes = (Response) in.readObject();
            assertTrue(loginRes.isSuccess());

            // Seller tạo Item (Thành công)
            com.auction.shared.model.entity.Electronics item = new com.auction.shared.model.entity.Electronics("ITEM_SELL_01", "MacBook Pro", 2000.0);
            item.setOwnerId("USR_SOCKET_SELLER");
            Request createReq = new Request(RequestType.CREATE_ITEM, item);
            out.writeObject(createReq);
            out.flush();
            Response createRes = (Response) in.readObject();
            assertTrue(createRes.isSuccess());

            // Seller xem các item của mình
            Request myItemsReq = new Request(RequestType.GET_MY_ITEMS, null);
            out.writeObject(myItemsReq);
            out.flush();
            Response myItemsRes = (Response) in.readObject();
            assertTrue(myItemsRes.isSuccess());

            // Seller lấy thông tin chi tiết một Item
            Request getItemReq = new Request(RequestType.GET_ITEM, "ITEM_SELL_01");
            out.writeObject(getItemReq);
            out.flush();
            Response getItemRes = (Response) in.readObject();
            assertTrue(getItemRes.isSuccess());

            // Seller cập nhật Item
            item.setDescription("New description");
            Request updateItemReq = new Request(RequestType.UPDATE_ITEM, item);
            out.writeObject(updateItemReq);
            out.flush();
            Response updateItemRes = (Response) in.readObject();
            assertTrue(updateItemRes.isSuccess());

            // Seller xóa Item
            Request deleteItemReq = new Request(RequestType.DELETE_ITEM, "ITEM_SELL_01");
            out.writeObject(deleteItemReq);
            out.flush();
            Response deleteItemRes = (Response) in.readObject();
            assertTrue(deleteItemRes.isSuccess());

            // Seller thử tạo Auction (Thất bại vì item chưa được APPROVED - mặc định là PENDING)
            com.auction.shared.model.entity.Auction auction = new com.auction.shared.model.entity.Auction();
            auction.setId("AUC_SELL_01");
            auction.setItem(item);
            Request createAucReq = new Request(RequestType.CREATE_AUCTION, auction);
            out.writeObject(createAucReq);
            out.flush();
            Response createAucRes = (Response) in.readObject();
            assertFalse(createAucRes.isSuccess());
        }
    }

    @Test
    @DisplayName("Test các hành động của Admin qua ClientHandler")
    void testClientHandler_AdminActions() throws Exception {
        com.auction.shared.model.entity.Admin admin = new com.auction.shared.model.entity.Admin("admin_sock_2", BCrypt.hashpw("admin123", BCrypt.gensalt()), "admin2@gmail.com");
        admin.setId("USR_SOCK_ADM");
        admin.setStatus(UserStatus.ACTIVE);
        userDAO.save(admin);

        try (Socket clientSocket = new Socket("localhost", port);
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

            out.flush();

            // Đăng nhập Admin
            Request loginReq = new Request(RequestType.LOGIN, new String[]{"admin_sock_2", "admin123"});
            out.writeObject(loginReq);
            out.flush();
            Response loginRes = (Response) in.readObject();
            assertTrue(loginRes.isSuccess());

            // Admin lấy tất cả Users
            Request getUsersReq = new Request(RequestType.GET_ALL_USERS, null);
            out.writeObject(getUsersReq);
            out.flush();
            Response getUsersRes = (Response) in.readObject();
            assertTrue(getUsersRes.isSuccess());

            // Admin khóa (Ban) một User không tồn tại (sẽ báo lỗi hoặc trả về thất bại)
            Request banReq = new Request(RequestType.BAN_USER, "INVALID_USER");
            out.writeObject(banReq);
            out.flush();
            Response banRes = (Response) in.readObject();
            assertFalse(banRes.isSuccess());
        }
    }
}
