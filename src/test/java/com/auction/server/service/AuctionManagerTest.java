package com.auction.server.service;

import com.auction.server.dao.*;
import com.auction.shared.model.entity.*;
import com.auction.shared.model.enums.*;
import com.auction.shared.protocol.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuctionManagerTest {

    private AuctionManager auctionManager;
    private UserDAO userDAO;
    private ItemDAO itemDAO;
    private AuctionDAO auctionDAO;

    @BeforeEach
    void setUp() throws Exception {
        auctionManager = AuctionManager.getInstance();
        userDAO = new UserDAO();
        itemDAO = new ItemDAO();
        auctionDAO = new AuctionDAO();

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement()) {
            // Cấu hình cấu trúc các bảng trong Database H2
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE, " +
                    "password VARCHAR(255), " +
                    "email VARCHAR(100), " +
                    "role VARCHAR(20), " +
                    "status VARCHAR(20))");

            stmt.execute("CREATE TABLE IF NOT EXISTS items (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "name VARCHAR(50), " +
                    "description VARCHAR(255), " +
                    "starting_price DOUBLE, " +
                    "item_type VARCHAR(20), " +
                    "warranty_months INT, " +
                    "mileage BIGINT, " +
                    "owner_id VARCHAR(50), " +
                    "status VARCHAR(20), " +
                    "image_bytes BLOB, " +
                    "preferred_start_time TIMESTAMP, " +
                    "preferred_end_time TIMESTAMP)");

            stmt.execute("CREATE TABLE IF NOT EXISTS auctions (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "item_id VARCHAR(50), " +
                    "current_price DOUBLE, " +
                    "current_winner_id VARCHAR(50), " +
                    "start_time TIMESTAMP, " +
                    "end_time TIMESTAMP, " +
                    "status VARCHAR(20))");

            stmt.execute("CREATE TABLE IF NOT EXISTS bid_history (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "auction_id VARCHAR(50), " +
                    "bidder_id VARCHAR(50), " +
                    "amount DOUBLE, " +
                    "timestamp TIMESTAMP)");

            stmt.execute("TRUNCATE TABLE bid_history");
            stmt.execute("TRUNCATE TABLE auctions");
            stmt.execute("TRUNCATE TABLE items");
            stmt.execute("TRUNCATE TABLE users");
        }
    }

    @Test
    @DisplayName("Test đăng ký Auto-bid thành công")
    void testRegisterAutoBid() throws Exception {
        // Tạo sản phẩm và phiên đấu giá
        Electronics item = new Electronics();
        item.setId("ITEM_AM_01");
        item.setName("Laptop");
        item.setStartingPrice(1000.0);
        itemDAO.save(item);

        Auction auction = new Auction();
        auction.setId("AUC_AM_01");
        auction.setItem(item);
        auction.setCurrentPrice(1000.0);
        auction.setStatus(AuctionStatus.RUNNING);
        auction.setStartTime(LocalDateTime.now());
        auction.setEndTime(LocalDateTime.now().plusDays(1));
        auctionDAO.save(auction);

        auctionManager.addAuction(auction);

        Response response = auctionManager.registerAutoBid("AUC_AM_01", "USER_AM_01", 1500.0);
        assertTrue(response.isSuccess());
        assertEquals("Cài đặt Auto-bid thành công.", response.getMessage());

        // Test đăng ký Auto-bid cho phiên đấu giá không tồn tại
        Response failResponse = auctionManager.registerAutoBid("INVALID_AUC", "USER_AM_01", 1500.0);
        assertFalse(failResponse.isSuccess());
    }

    @Test
    @DisplayName("Test xử lý đặt giá mới thành công")
    void testProcessNewBid_Success() throws Exception {
        // Lưu bidder vào Database
        Bidder bidder = new Bidder("bidder1", "pass", "b1@gmail.com");
        bidder.setId("BIDDER_AM_01");
        userDAO.save(bidder);

        // Lưu sản phẩm và phiên đấu giá
        Electronics item = new Electronics();
        item.setId("ITEM_AM_02");
        item.setName("Phone");
        item.setStartingPrice(500.0);
        itemDAO.save(item);

        Auction auction = new Auction();
        auction.setId("AUC_AM_02");
        auction.setItem(item);
        auction.setCurrentPrice(500.0);
        auction.setStatus(AuctionStatus.RUNNING);
        auction.setStartTime(LocalDateTime.now());
        auction.setEndTime(LocalDateTime.now().plusDays(1));
        auctionDAO.save(auction);

        auctionManager.addAuction(auction);

        // Xử lý một lượt đặt giá hợp lệ
        Response response = auctionManager.processNewBid("AUC_AM_02", "BIDDER_AM_01", 600.0);
        assertTrue(response.isSuccess());
        assertEquals("Đặt giá thành công!", response.getMessage());

        // Kiểm tra cơ sở dữ liệu đã được cập nhật
        Auction updated = auctionDAO.findById("AUC_AM_02");
        assertEquals(600.0, updated.getCurrentPrice());
        assertEquals("BIDDER_AM_01", updated.getCurrentWinner().getId());
    }

    @Test
    @DisplayName("Test BVA logic Anti-sniping: Gia hạn khi trong biên [0, 300) giây")
    void testAntiSnipingLogic_BVA_Extend() throws Exception {
        Bidder bidder = new Bidder("bidder2", "pass", "b2@gmail.com");
        bidder.setId("BIDDER_AM_02");
        userDAO.save(bidder);

        Electronics item = new Electronics();
        item.setId("ITEM_AM_03");
        item.setName("Tablet");
        item.setStartingPrice(300.0);
        itemDAO.save(item);

        // Biên trên hợp lệ: còn đúng 299 giây (hoặc ít hơn do trễ thực thi)
        LocalDateTime initialEndTime = LocalDateTime.now().plusSeconds(299);

        Auction auction = new Auction();
        auction.setId("AUC_AM_03");
        auction.setItem(item);
        auction.setCurrentPrice(300.0);
        auction.setStatus(AuctionStatus.RUNNING);
        auction.setStartTime(LocalDateTime.now().minusHours(1));
        auction.setEndTime(initialEndTime);
        auctionDAO.save(auction);

        auctionManager.addAuction(auction);

        // Đặt giá thầu (cần đặt lớn hơn currentPrice + minStep. minStep của 300.0 là max(1.0, 3.0) = 3.0. Nên đặt 310.0)
        Response response = auctionManager.processNewBid("AUC_AM_03", "BIDDER_AM_02", 310.0);
        assertTrue(response.isSuccess());

        // Kiểm tra xem thời gian kết thúc có được kéo dài thêm 5 phút (300 giây) hay không
        Auction updated = auctionDAO.findById("AUC_AM_03");
        assertTrue(updated.getEndTime().isAfter(initialEndTime));
    }

    @Test
    @DisplayName("Test BVA logic Anti-sniping: Không gia hạn khi ngoài biên (>= 300 giây)")
    void testAntiSnipingLogic_BVA_NoExtend() throws Exception {
        Bidder bidder = new Bidder("bidder3", "pass", "b3@gmail.com");
        bidder.setId("BIDDER_AM_03");
        userDAO.save(bidder);

        Electronics item = new Electronics();
        item.setId("ITEM_AM_04");
        item.setName("Tablet 2");
        item.setStartingPrice(300.0);
        itemDAO.save(item);

        // Ngoài biên trên: còn 305 giây
        LocalDateTime initialEndTime = LocalDateTime.now().plusSeconds(305);

        Auction auction = new Auction();
        auction.setId("AUC_AM_04");
        auction.setItem(item);
        auction.setCurrentPrice(300.0);
        auction.setStatus(AuctionStatus.RUNNING);
        auction.setStartTime(LocalDateTime.now().minusHours(1));
        auction.setEndTime(initialEndTime);
        auctionDAO.save(auction);

        auctionManager.addAuction(auction);

        // Đặt giá thầu
        Response response = auctionManager.processNewBid("AUC_AM_04", "BIDDER_AM_03", 310.0);
        assertTrue(response.isSuccess());

        // Kiểm tra xem thời gian kết thúc giữ nguyên
        Auction updated = auctionDAO.findById("AUC_AM_04");
        assertEquals(initialEndTime.getSecond(), updated.getEndTime().getSecond());
    }

    @Test
    @DisplayName("Test kết thúc phiên đấu giá và thông báo tới các observer")
    void testEndAuction() throws Exception {
        Electronics item = new Electronics();
        item.setId("ITEM_AM_04");
        item.setName("Watch");
        item.setStartingPrice(200.0);
        itemDAO.save(item);

        Auction auction = new Auction();
        auction.setId("AUC_AM_04");
        auction.setItem(item);
        auction.setCurrentPrice(200.0);
        auction.setStatus(AuctionStatus.RUNNING);
        auctionDAO.save(auction);

        auctionManager.addAuction(auction);

        // Đăng ký observer giả lập
        final boolean[] notified = {false};
        BidObserver observer = (item1, currentPrice, winnerId, endTime) -> {
            notified[0] = true;
        };
        auctionManager.addObserver(observer);

        // Kết thúc phiên đấu giá
        auctionManager.endAuction("AUC_AM_04");

        // Kiểm chứng trạng thái
        Auction updated = auctionDAO.findById("AUC_AM_04");
        assertEquals(AuctionStatus.FINISHED, updated.getStatus());
        assertTrue(notified[0]);

        // Dọn dẹp và hủy đăng ký observer
        auctionManager.removeObserver(observer);
    }

    @Test
    @DisplayName("Test Auto-bid trigger thành công khi có người đặt giá mới")
    void testAutoBidTrigger_Success() throws Exception {
        // 1. Tạo và lưu 2 bidder
        Bidder bidderA = new Bidder("bidderA", "pass", "a@gmail.com");
        bidderA.setId("USER_AM_AUTO_01");
        userDAO.save(bidderA);

        Bidder bidderB = new Bidder("bidderB", "pass", "b@gmail.com");
        bidderB.setId("USER_AM_AUTO_02");
        userDAO.save(bidderB);

        // 2. Tạo sản phẩm và phiên đấu giá
        Electronics item = new Electronics();
        item.setId("ITEM_AM_AUTO_01");
        item.setName("SmartTV");
        item.setStartingPrice(500000.0);
        itemDAO.save(item);

        Auction auction = new Auction();
        auction.setId("AUC_AM_AUTO_01");
        auction.setItem(item);
        auction.setCurrentPrice(500000.0);
        auction.setStatus(AuctionStatus.RUNNING);
        auction.setStartTime(LocalDateTime.now());
        auction.setEndTime(LocalDateTime.now().plusDays(1));
        auctionDAO.save(auction);

        auctionManager.addAuction(auction);

        // 3. Đăng ký Auto-bid cho Bidder A với maxAmount = 1500000
        Response regResponse = auctionManager.registerAutoBid("AUC_AM_AUTO_01", "USER_AM_AUTO_01", 1500000.0);
        assertTrue(regResponse.isSuccess());

        // 4. Bidder B đặt giá thủ công 600000
        Response bidResponse = auctionManager.processNewBid("AUC_AM_AUTO_01", "USER_AM_AUTO_02", 600000.0);
        assertTrue(bidResponse.isSuccess());

        // 5. Chờ luồng Auto-bid xử lý bất đồng bộ
        boolean autoBidExecuted = false;
        for (int i = 0; i < 20; i++) {
            Thread.sleep(50);
            Auction updated = auctionDAO.findById("AUC_AM_AUTO_01");
            if (updated.getCurrentPrice() == 605000.0 && "USER_AM_AUTO_01".equals(updated.getCurrentWinner().getId())) {
                autoBidExecuted = true;
                break;
            }
        }

        assertTrue(autoBidExecuted, "Auto-bid phải tự động được kích hoạt và nâng giá lên 605,000 VND");
    }

    @Test
    @DisplayName("Test BVA Auto-bid không kích hoạt khi maxAmount sát biên dưới của nextBid (BVA: maxAmount = nextBid - 0.01)")
    void testAutoBidTrigger_BVA_LimitNotReached() throws Exception {
        // 1. Tạo và lưu 2 bidder
        Bidder bidderA = new Bidder("bidderA2", "pass", "a2@gmail.com");
        bidderA.setId("USER_AM_AUTO_03");
        userDAO.save(bidderA);

        Bidder bidderB = new Bidder("bidderB2", "pass", "b2@gmail.com");
        bidderB.setId("USER_AM_AUTO_04");
        userDAO.save(bidderB);

        // 2. Tạo sản phẩm và phiên đấu giá
        Electronics item = new Electronics();
        item.setId("ITEM_AM_AUTO_02");
        item.setName("Tablet");
        item.setStartingPrice(500000.0);
        itemDAO.save(item);

        Auction auction = new Auction();
        auction.setId("AUC_AM_AUTO_02");
        auction.setItem(item);
        auction.setCurrentPrice(500000.0);
        auction.setStatus(AuctionStatus.RUNNING);
        auction.setStartTime(LocalDateTime.now());
        auction.setEndTime(LocalDateTime.now().plusDays(1));
        auctionDAO.save(auction);

        auctionManager.addAuction(auction);

        // 3. Đăng ký Auto-bid cho Bidder A với maxAmount = 604999.99 (nhỏ hơn mức giá tiếp theo 605000.0 đúng 0.01)
        auctionManager.registerAutoBid("AUC_AM_AUTO_02", "USER_AM_AUTO_03", 604999.99);

        // 4. Bidder B đặt giá thủ công 600000 (giá tiếp theo mong đợi là 605000)
        Response bidResponse = auctionManager.processNewBid("AUC_AM_AUTO_02", "USER_AM_AUTO_04", 600000.0);
        assertTrue(bidResponse.isSuccess());

        // 5. Chờ xem Auto-bid có bị kích hoạt không (không được kích hoạt)
        Thread.sleep(300);
        Auction updated = auctionDAO.findById("AUC_AM_AUTO_02");
        assertEquals(600000.0, updated.getCurrentPrice());
        assertEquals("USER_AM_AUTO_04", updated.getCurrentWinner().getId());
    }

    @Test
    @DisplayName("Test BVA Auto-bid kích hoạt thành công khi maxAmount bằng đúng nextBid (BVA: maxAmount = nextBid)")
    void testAutoBidTrigger_BVA_ExactlyLimit() throws Exception {
        // 1. Tạo và lưu 2 bidder
        Bidder bidderA = new Bidder("bidderA2_exact", "pass", "a2exact@gmail.com");
        bidderA.setId("USER_AM_AUTO_03_EXACT");
        userDAO.save(bidderA);

        Bidder bidderB = new Bidder("bidderB2_exact", "pass", "b2exact@gmail.com");
        bidderB.setId("USER_AM_AUTO_04_EXACT");
        userDAO.save(bidderB);

        // 2. Tạo sản phẩm và phiên đấu giá
        Electronics item = new Electronics();
        item.setId("ITEM_AM_AUTO_02_EXACT");
        item.setName("Tablet Exact");
        item.setStartingPrice(500000.0);
        itemDAO.save(item);

        Auction auction = new Auction();
        auction.setId("AUC_AM_AUTO_02_EXACT");
        auction.setItem(item);
        auction.setCurrentPrice(500000.0);
        auction.setStatus(AuctionStatus.RUNNING);
        auction.setStartTime(LocalDateTime.now());
        auction.setEndTime(LocalDateTime.now().plusDays(1));
        auctionDAO.save(auction);

        auctionManager.addAuction(auction);

        // 3. Đăng ký Auto-bid cho Bidder A với maxAmount = 605000.0 (bằng đúng nextBid 605000.0)
        auctionManager.registerAutoBid("AUC_AM_AUTO_02_EXACT", "USER_AM_AUTO_03_EXACT", 605000.0);

        // 4. Bidder B đặt giá thủ công 600000
        Response bidResponse = auctionManager.processNewBid("AUC_AM_AUTO_02_EXACT", "USER_AM_AUTO_04_EXACT", 600000.0);
        assertTrue(bidResponse.isSuccess());

        // 5. Chờ luồng Auto-bid xử lý bất đồng bộ
        boolean autoBidExecuted = false;
        for (int i = 0; i < 20; i++) {
            Thread.sleep(50);
            Auction updated = auctionDAO.findById("AUC_AM_AUTO_02_EXACT");
            if (updated.getCurrentPrice() == 605000.0 && "USER_AM_AUTO_03_EXACT".equals(updated.getCurrentWinner().getId())) {
                autoBidExecuted = true;
                break;
            }
        }

        assertTrue(autoBidExecuted, "Auto-bid phải tự động được kích hoạt vì maxAmount bằng đúng nextBid");
    }

    @Test
    @DisplayName("Test Auto-bid ping-pong giữa 2 người cài cấu hình Auto-bid")
    void testAutoBidTrigger_PingPong() throws Exception {
        // 1. Tạo và lưu 3 bidder
        Bidder bidderA = new Bidder("bidderA3", "pass", "a3@gmail.com");
        bidderA.setId("USER_AM_AUTO_05");
        userDAO.save(bidderA);

        Bidder bidderB = new Bidder("bidderB3", "pass", "b3@gmail.com");
        bidderB.setId("USER_AM_AUTO_06");
        userDAO.save(bidderB);

        Bidder bidderC = new Bidder("bidderC3", "pass", "c3@gmail.com");
        bidderC.setId("USER_AM_AUTO_07");
        userDAO.save(bidderC);

        // 2. Tạo sản phẩm và phiên đấu giá
        Electronics item = new Electronics();
        item.setId("ITEM_AM_AUTO_03");
        item.setName("Console");
        item.setStartingPrice(5000000.0);
        itemDAO.save(item);

        Auction auction = new Auction();
        auction.setId("AUC_AM_AUTO_03");
        auction.setItem(item);
        auction.setCurrentPrice(5000000.0);
        auction.setStatus(AuctionStatus.RUNNING);
        auction.setStartTime(LocalDateTime.now());
        auction.setEndTime(LocalDateTime.now().plusDays(1));
        auctionDAO.save(auction);

        auctionManager.addAuction(auction);

        // 3. Đăng ký Auto-bid cho Bidder A (max 5,550,000) và Bidder B (max 5,600,000)
        auctionManager.registerAutoBid("AUC_AM_AUTO_03", "USER_AM_AUTO_05", 5550000.0);
        auctionManager.registerAutoBid("AUC_AM_AUTO_03", "USER_AM_AUTO_06", 5600000.0);

        // 4. Bidder C đặt giá thủ công 550000 để kích hoạt ping-pong
        Response bidResponse = auctionManager.processNewBid("AUC_AM_AUTO_03", "USER_AM_AUTO_07", 5500000.0);
        assertTrue(bidResponse.isSuccess());

        // 5. Chờ chuỗi ping-pong tự nâng giá hoàn thành
        boolean pingPongDone = false;
        for (int i = 0; i < 100; i++) { // Increase wait iterations because smaller step means more bids
            Thread.sleep(100);
            Auction updated = auctionDAO.findById("AUC_AM_AUTO_03");
            if (updated.getCurrentPrice() == 5550000.0 && "USER_AM_AUTO_06".equals(updated.getCurrentWinner().getId())) {
                pingPongDone = true;
                break;
            }
        }

        assertTrue(pingPongDone, "Chuỗi ping-pong Auto-bid phải dừng ở 5,550,000 VND và người thắng là Bidder B");
    }
}
