package com.auction.server.dao;

import com.auction.shared.model.entity.BidTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BidTransactionDAOIntegrationTest {

    private BidTransactionDAO bidTransactionDAO;

    @BeforeEach
    void setUp() throws Exception {
        bidTransactionDAO = new BidTransactionDAO();

        Connection conn = DatabaseConnection.getInstance().getConnection();
        Statement stmt = conn.createStatement();

        stmt.execute("CREATE TABLE IF NOT EXISTS bid_history (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "auction_id VARCHAR(50), " +
                "bidder_id VARCHAR(50), " +
                "amount DOUBLE, " +
                "timestamp TIMESTAMP)");
        stmt.execute("TRUNCATE TABLE bid_history");
    }

    @Test
    @DisplayName("Test luồng thật: Lưu và Lấy lịch sử đấu giá")
    void testSaveAndGetBidsByAuctionId_RealDB() throws Exception {
        BidTransaction tx1 = new BidTransaction();
        tx1.setId("TX_REAL_01");
        tx1.setAuctionId("AUC_REAL_01");
        tx1.setBidderId("USER_REAL_WIN");
        tx1.setAmount(100.0);
        tx1.setTimestamp(LocalDateTime.now().minusMinutes(5));

        BidTransaction tx2 = new BidTransaction();
        tx2.setId("TX_REAL_02");
        tx2.setAuctionId("AUC_REAL_01");
        tx2.setBidderId("USER_REAL_WIN");
        tx2.setAmount(150.0);
        tx2.setTimestamp(LocalDateTime.now());

        assertDoesNotThrow(() -> bidTransactionDAO.save(tx1));
        assertDoesNotThrow(() -> bidTransactionDAO.save(tx2));

        List<BidTransaction> history = bidTransactionDAO.getBidsByAuctionId("AUC_REAL_01");
        assertEquals(2, history.size());
        assertEquals("TX_REAL_01", history.get(0).getId());
        assertEquals("TX_REAL_02", history.get(1).getId());
    }
}
