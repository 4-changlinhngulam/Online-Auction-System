package com.auction.server.dao;

import com.auction.shared.exception.DataPersistenceException;
import com.auction.shared.model.entity.BidTransaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Quản lý các giao dịch đấu giá vào MySQL.
 */
public class BidTransactionDAO {

    private static final Logger LOGGER = Logger.getLogger(BidTransactionDAO.class.getName());

    public void save(BidTransaction transaction) throws DataPersistenceException {
        if (transaction == null || transaction.getId() == null) {
            throw new IllegalArgumentException("BidTransaction và ID không được null");
        }

        String sql = "INSERT INTO bid_history (id, auction_id, bidder_id, amount, timestamp) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, transaction.getId());
            pstmt.setString(2, transaction.getAuctionId());
            pstmt.setString(3, transaction.getBidderId());
            pstmt.setDouble(4, transaction.getAmount());
            pstmt.setTimestamp(5, Timestamp.valueOf(transaction.getTimestamp()));

            pstmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lưu BidTransaction vào DB: " + e.getMessage(), e);
            throw new DataPersistenceException("Lỗi lưu BidTransaction", e);
        }
    }

    /**
     * Lấy toàn bộ lịch sử đấu giá của một phiên cụ thể.
     * Sắp xếp theo thời gian từ cũ đến mới (ASC).
     */
    public List<BidTransaction> getBidsByAuctionId(String auctionId) {
        List<BidTransaction> list = new ArrayList<>();
        String sql = "SELECT b.*, u.username FROM bid_history b " +
                     "JOIN users u ON b.bidder_id = u.id " +
                     "WHERE b.auction_id = ? ORDER BY b.timestamp ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, auctionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BidTransaction tx = new BidTransaction();
                    tx.setId(rs.getString("id"));
                    tx.setAuctionId(rs.getString("auction_id"));
                    tx.setBidderId(rs.getString("username")); // Đặt tạm username vào bidderId để Client hiển thị
                    tx.setAmount(rs.getDouble("amount"));
                    if (rs.getTimestamp("timestamp") != null) {
                        tx.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                    }
                    list.add(tx);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy lịch sử đấu giá: " + e.getMessage(), e);
        }
        return list;
    }
}
