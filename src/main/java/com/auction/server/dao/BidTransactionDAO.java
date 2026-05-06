package com.auction.server.dao;

import com.auction.shared.model.entity.BidTransaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class BidTransactionDAO {

    /**
     * Lưu một giao dịch đặt giá mới vào Database.
     */
    public boolean save(BidTransaction bidTransaction) {
        // Cấu trúc bảng MySQL vẫn giữ nguyên như cũ
        String sql = "INSERT INTO bid_transactions (id, auction_id, bidder_id, bid_amount, bid_time) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Khớp với các hàm getter từ BidTransaction.java
            pstmt.setString(1, bidTransaction.getId()); // ID kế thừa từ lớp Entity
            pstmt.setString(2, bidTransaction.getAuctionId());
            pstmt.setString(3, bidTransaction.getBidderId());
            pstmt.setDouble(4, bidTransaction.getAmount());

            // Chuyển LocalDateTime thành Timestamp cho MySQL
            pstmt.setTimestamp(5, Timestamp.valueOf(bidTransaction.getTimestamp()));

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi lưu BidTransaction vào DB: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy danh sách lịch sử đặt giá của một phiên đấu giá cụ thể.
     */
    public List<BidTransaction> getBidsByAuctionId(String auctionId) {
        List<BidTransaction> bidHistory = new ArrayList<>();

        // Không cần JOIN với bảng users nữa vì class BidTransaction không có chỗ chứa username
        String sql = "SELECT * FROM bid_transactions WHERE auction_id = ? ORDER BY bid_time DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, auctionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BidTransaction bid = new BidTransaction();

                    // Lấy dữ liệu từ Database và dùng setter tương ứng
                    bid.setId(rs.getString("id"));
                    bid.setAuctionId(rs.getString("auction_id"));
                    bid.setBidderId(rs.getString("bidder_id"));
                    bid.setAmount(rs.getDouble("bid_amount"));
                    bid.setTimestamp(rs.getTimestamp("bid_time").toLocalDateTime());

                    bidHistory.add(bid);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy lịch sử đấu giá: " + e.getMessage());
        }

        return bidHistory;
    }
}