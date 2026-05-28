package com.auction.server.dao;

import com.auction.server.service.ItemFactory;
import com.auction.shared.exception.DataPersistenceException;
import com.auction.shared.exception.EntityNotFoundException;
import com.auction.shared.model.entity.Auction;
import com.auction.shared.model.entity.Bidder;
import com.auction.shared.model.entity.Item;
import com.auction.shared.model.enums.AuctionStatus;
import com.auction.shared.model.enums.ItemType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO cho Auction. Kết nối trực tiếp với MySQL trên Cloud.
 */
public class AuctionDAO {

    private static final Logger LOGGER = Logger.getLogger(AuctionDAO.class.getName());

    // 1. HÀM THÊM MỚI (SAVE)

    public void save(Auction auction) throws DataPersistenceException {
        if (auction == null || auction.getId() == null) {
            throw new IllegalArgumentException("Auction và ID không được phép null");
        }

        String sql = "INSERT INTO auctions " +
                "(id, item_id, current_price, current_winner_id, start_time, end_time, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, auction.getId());

            // Xử lý Khóa ngoại: Lấy ID của Item
            if (auction.getItem() == null) {
                throw new IllegalArgumentException("Phiên đấu giá phải có Item đính kèm!");
            }
            pstmt.setString(2, auction.getItem().getId());

            pstmt.setDouble(3, auction.getCurrentPrice());

            // Xử lý người thắng hiện tại (Có thể null nếu chưa ai đấu giá)
            if (auction.getCurrentWinner() != null) {
                pstmt.setString(4, auction.getCurrentWinner().getId());
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }

            // Đổi LocalDateTime trong Java thành Timestamp trong MySQL
            pstmt.setTimestamp(5, auction.getStartTime() != null ? Timestamp.valueOf(auction.getStartTime()) : null);
            pstmt.setTimestamp(6, auction.getEndTime() != null ? Timestamp.valueOf(auction.getEndTime()) : null);

            // Lưu Enum dưới dạng chuỗi (String)
            pstmt.setString(7, auction.getStatus() != null ? auction.getStatus().name() : AuctionStatus.OPEN.name());

            pstmt.executeUpdate();
            LOGGER.log(Level.INFO, "Đã lưu phiên đấu giá {0} lên Database!", auction.getId());

        } catch (SQLIntegrityConstraintViolationException e) {
            // MySQL tự động chặn ID trùng lặp
            throw new IllegalArgumentException(
                    "Từ chối thêm mới: Phiên đấu giá ID '" + auction.getId() + "' đã tồn tại!");
        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi hệ thống khi lưu Auction vào Database", e);
        }
    }

    // 2. HÀM CẬP NHẬT (UPDATE)

    public void update(Auction auction) throws DataPersistenceException, EntityNotFoundException {
        if (auction == null || auction.getId() == null) {
            throw new IllegalArgumentException("Auction và ID không được phép null");
        }

        String sql = "UPDATE auctions " +
                "SET item_id = ?, current_price = ?, current_winner_id = ?, start_time = ?, end_time = ?, status = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, auction.getItem().getId());
            pstmt.setDouble(2, auction.getCurrentPrice());

            if (auction.getCurrentWinner() != null) {
                pstmt.setString(3, auction.getCurrentWinner().getId());
            } else {
                pstmt.setNull(3, Types.VARCHAR);
            }

            pstmt.setTimestamp(4, auction.getStartTime() != null ? Timestamp.valueOf(auction.getStartTime()) : null);
            pstmt.setTimestamp(5, auction.getEndTime() != null ? Timestamp.valueOf(auction.getEndTime()) : null);
            pstmt.setString(6, auction.getStatus().name());

            pstmt.setString(7, auction.getId()); // Đặt ID cho điều kiện WHERE

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new EntityNotFoundException(
                        "Không thể cập nhật: Không tìm thấy phiên đấu giá ID '" + auction.getId() + "'");
            }

        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi khi cập nhật Auction trên Database", e);
        }
    }

    // 3. LẤY TẤT CẢ PHIÊN ĐẤU GIÁ (FIND ALL)

    public List<Auction> findAll() throws DataPersistenceException {
        List<Auction> auctions = new ArrayList<>();
        // - Lỗi N+1 Query cũ: Dùng vòng lặp while(rs.next()) gọi thêm 2 câu SELECT
        // (findById) làm chậm hệ thống.
        // - Khắc phục: Sử dụng JOIN (INNER JOIN items, LEFT JOIN users) để lấy toàn bộ
        // dữ liệu trong 1 câu truy vấn.
        String sql = "SELECT a.*, i.name as item_name, i.description as item_desc, i.starting_price, i.item_type, "
                + "u.username as winner_username "
                + "FROM auctions a "
                + "JOIN items i ON a.item_id = i.id "
                + "LEFT JOIN users u ON a.current_winner_id = u.id";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                auctions.add(mapResultSetToAuction(rs));
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi khi đọc danh sách Auction từ DB", e);
        }
        return auctions;
    }

    // 4. TÌM THEO ID (FIND BY ID)

    public Auction findById(String id) throws DataPersistenceException, EntityNotFoundException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID tìm kiếm không hợp lệ.");
        }

        // Tối ưu N+1 Query tương tự như findAll
        String sql = "SELECT a.*, i.name as item_name, i.description as item_desc, i.starting_price, i.item_type, "
                + "u.username as winner_username "
                + "FROM auctions a "
                + "JOIN items i ON a.item_id = i.id "
                + "LEFT JOIN users u ON a.current_winner_id = u.id "
                + "WHERE a.id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAuction(rs);
                } else {
                    throw new EntityNotFoundException("Không tìm thấy phiên đấu giá có ID: " + id);
                }
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi khi tìm phiên đấu giá theo ID", e);
        }
    }

    // 5. XÓA (DELETE)

    public void delete(String id) throws DataPersistenceException, EntityNotFoundException {
        String sql = "DELETE FROM auctions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new EntityNotFoundException("Không thể xóa: Không tìm thấy phiên đấu giá ID '" + id + "'");
            }

        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi khi xóa Auction khỏi Database", e);
        }
    }

    // 6. LẤY CÁC PHIÊN ĐẤU GIÁ MỞ (getOpenAuctions)
    public List<Auction> getOpenAuctions() {
        List<Auction> openAuctions = new ArrayList<>();
        String sql = "SELECT a.*, i.name as item_name, i.description as item_desc, i.starting_price, i.item_type, "
                + "u.username as winner_username "
                + "FROM auctions a "
                + "JOIN items i ON a.item_id = i.id "
                + "LEFT JOIN users u ON a.current_winner_id = u.id "
                + "WHERE a.status IN ('OPEN', 'RUNNING')";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                openAuctions.add(mapResultSetToAuction(rs));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy danh sách phiên đấu giá đang mở: " + e.getMessage(), e);
        }

        return openAuctions;
    }

    // --- HÀM HỖ TRỢ: CHUYỂN ĐỔI DỮ LIỆU TỪ DB SANG JAVA OBJECT ---
    private Auction mapResultSetToAuction(ResultSet rs) throws SQLException {
        Auction auction = new Auction();
        auction.setId(rs.getString("id"));
        auction.setCurrentPrice(rs.getDouble("current_price"));
        auction.setStatus(AuctionStatus.valueOf(rs.getString("status")));

        if (rs.getTimestamp("start_time") != null) {
            auction.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        }
        if (rs.getTimestamp("end_time") != null) {
            auction.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        }

        // --- Xử lý ghép nối Item trực tiếp từ ResultSet JOIN ---
        String itemTypeStr = rs.getString("item_type");
        ItemType itemType = ItemType.valueOf(itemTypeStr);
        String itemId = rs.getString("item_id");
        String itemName = rs.getString("item_name");
        double startingPrice = rs.getDouble("starting_price");

        Item item = ItemFactory.createItem(itemType, itemId, itemName, startingPrice);
        item.setDescription(rs.getString("item_desc"));

        auction.setItem(item);

        // --- Xử lý ghép nối Winner trực tiếp từ ResultSet JOIN ---
        String winnerId = rs.getString("current_winner_id");
        if (winnerId != null) {
            Bidder winner = new Bidder();
            winner.setId(winnerId);
            winner.setName(rs.getString("winner_username"));
            auction.setCurrentWinner(winner);
        }

        return auction;
    }
}
