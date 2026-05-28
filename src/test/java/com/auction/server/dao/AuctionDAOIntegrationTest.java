package com.auction.server.dao;

import com.auction.shared.model.entity.Auction;
import com.auction.shared.model.entity.Bidder;
import com.auction.shared.model.entity.Electronics;
import com.auction.shared.model.entity.Item;
import com.auction.shared.model.enums.AuctionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuctionDAOIntegrationTest {

    private AuctionDAO auctionDAO;
    private ItemDAO itemDAO;
    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws Exception {
        auctionDAO = new AuctionDAO();
        itemDAO = new ItemDAO();
        userDAO = new UserDAO();

        Connection conn = DatabaseConnection.getInstance().getConnection();
        Statement stmt = conn.createStatement();

        // 1. Tạo bảng users
        stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "username VARCHAR(50) UNIQUE, " +
                "password VARCHAR(255), " +
                "email VARCHAR(100), " +
                "role VARCHAR(20), " +
                "status VARCHAR(20))");

        // 2. Tạo bảng items
        stmt.execute("CREATE TABLE IF NOT EXISTS items (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "name VARCHAR(50), " +
                "description VARCHAR(255), " +
                "starting_price DOUBLE, " +
                "item_type VARCHAR(20), " +
                "warranty_months INT, " +
                "mileage BIGINT)");

        // 3. Tạo bảng auctions
        stmt.execute("CREATE TABLE IF NOT EXISTS auctions (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "item_id VARCHAR(50), " +
                "current_price DOUBLE, " +
                "current_winner_id VARCHAR(50), " +
                "start_time TIMESTAMP, " +
                "end_time TIMESTAMP, " +
                "status VARCHAR(20))");

        // Dọn sạch bảng
        stmt.execute("TRUNCATE TABLE auctions");
        stmt.execute("TRUNCATE TABLE items");
        stmt.execute("TRUNCATE TABLE users");
    }

    @Test
    @DisplayName("Test luồng thật: Lưu và Tìm Auction bằng ID")
    void testSaveAndFindById_RealDB() throws Exception {
        // Tạo User winner
        Bidder winner = new Bidder("winner_real", "pass", "winner@gmail.com");
        winner.setId("USER_REAL_WIN");
        userDAO.save(winner);

        // Tạo Item
        Electronics item = new Electronics();
        item.setId("ITEM_REAL_AUC");
        item.setName("SmartTV");
        item.setDescription("Samsung 4K");
        item.setStartingPrice(800.0);
        item.setWarrantyMonths(24);
        itemDAO.save(item);

        // Tạo Auction
        Auction auction = new Auction();
        auction.setId("AUC_REAL_01");
        auction.setItem(item);
        auction.setCurrentPrice(850.0);
        auction.setCurrentWinner(winner);
        auction.setStartTime(LocalDateTime.now());
        auction.setEndTime(LocalDateTime.now().plusDays(2));
        auction.setStatus(AuctionStatus.OPEN);

        assertDoesNotThrow(() -> auctionDAO.save(auction));

        Auction fetched = auctionDAO.findById("AUC_REAL_01");
        assertNotNull(fetched);
        assertEquals("AUC_REAL_01", fetched.getId());
        assertEquals(850.0, fetched.getCurrentPrice());
        assertEquals("SmartTV", fetched.getItem().getName());
        assertEquals("winner_real", fetched.getCurrentWinner().getName());
    }

    @Test
    @DisplayName("Test luồng thật: Cập nhật Auction")
    void testUpdate_RealDB() throws Exception {
        Electronics item = new Electronics();
        item.setId("ITEM_REAL_AUC2");
        item.setName("TV2");
        item.setStartingPrice(500.0);
        itemDAO.save(item);

        Auction auction = new Auction();
        auction.setId("AUC_REAL_02");
        auction.setItem(item);
        auction.setCurrentPrice(500.0);
        auction.setStatus(AuctionStatus.OPEN);
        auctionDAO.save(auction);

        auction.setCurrentPrice(600.0);
        auction.setStatus(AuctionStatus.FINISHED);
        auctionDAO.update(auction);

        Auction fetched = auctionDAO.findById("AUC_REAL_02");
        assertEquals(600.0, fetched.getCurrentPrice());
        assertEquals(AuctionStatus.FINISHED, fetched.getStatus());
    }

    @Test
    @DisplayName("Test luồng thật: Lấy danh sách Auction đang mở")
    void testGetOpenAuctions_RealDB() throws Exception {
        Electronics item = new Electronics();
        item.setId("ITEM_REAL_AUC3");
        item.setName("TV3");
        itemDAO.save(item);

        Auction auction1 = new Auction();
        auction1.setId("AUC_REAL_03");
        auction1.setItem(item);
        auction1.setStatus(AuctionStatus.OPEN);
        auctionDAO.save(auction1);

        Auction auction2 = new Auction();
        auction2.setId("AUC_REAL_04");
        auction2.setItem(item);
        auction2.setStatus(AuctionStatus.FINISHED);
        auctionDAO.save(auction2);

        List<Auction> openAuctions = auctionDAO.getOpenAuctions();
        assertFalse(openAuctions.isEmpty());
        boolean found = openAuctions.stream().anyMatch(a -> a.getId().equals("AUC_REAL_03"));
        assertTrue(found);
        boolean notFoundClosed = openAuctions.stream().noneMatch(a -> a.getId().equals("AUC_REAL_04"));
        assertTrue(notFoundClosed);
    }
}
