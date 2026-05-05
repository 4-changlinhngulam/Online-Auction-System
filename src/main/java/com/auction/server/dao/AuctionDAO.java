package com.auction.server.dao;

import com.auction.shared.model.entity.Auction;
import com.auction.shared.model.entity.Bidder;
import com.auction.shared.model.entity.Item;
import com.auction.shared.model.entity.User;
import com.auction.shared.model.enums.AuctionStatus;
import com.auction.shared.exception.DataPersistenceException;
import com.auction.shared.exception.EntityNotFoundException;
import com.auction.server.service.ItemFactory;
import com.auction.shared.model.enums.ItemType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho Auction. Kết nối trực tiếp với MySQL trên Cloud.
 */
public class AuctionDAO {


    // 1. HÀM THÊM MỚI (SAVE)

    public void save(Auction auction) throws DataPersistenceException {
        if (auction == null || auction.getId() == null) {
            throw new IllegalArgumentException("Auction và ID không được phép null");
        }

        String sql = "INSERT INTO auctions (id, item_id, current_price, current_winner_id, start_time, end_time, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, auction.getId());

            // Xử lý Khóa ngoại: Lấy ID của Item (Nếu item bị null thì báo lỗi ngay)
            if (auction.getItem() == null) throw new IllegalArgumentException("Phiên đấu giá phải có Item đính kèm!");
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
            System.out.println("xĐã lưu phiên đấu giá " + auction.getId() + " lên Cloud!");

        } catch (SQLIntegrityConstraintViolationException e) {
            // MySQL tự động chặn ID trùng lặp
            throw new IllegalArgumentException("Từ chối thêm mới: Phiên đấu giá ID '" + auction.getId() + "' đã tồn tại!");
        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi hệ thống khi lưu Auction vào Database", e);
        }
    }


    // 2. HÀM CẬP NHẬT (UPDATE)

    public void update(Auction auction) throws DataPersistenceException, EntityNotFoundException {
        if (auction == null || auction.getId() == null) {
            throw new IllegalArgumentException("Auction và ID không được phép null");
        }

        String sql = "UPDATE auctions SET item_id = ?, current_price = ?, current_winner_id = ?, start_time = ?, end_time = ?, status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance();
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
                throw new EntityNotFoundException("Không thể cập nhật: Không tìm thấy phiên đấu giá ID '" + auction.getId() + "'");
            }

        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi khi cập nhật Auction trên Database", e);
        }
    }


    // 3. LẤY TẤT CẢ PHIÊN ĐẤU GIÁ (FIND ALL)

    public List<Auction> findAll() throws DataPersistenceException {
        List<Auction> auctions = new ArrayList<>();
        String sql = "SELECT * FROM auctions";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            // Để lấy được Item và User đầy đủ, chúng ta nhờ ItemDAO và UserDAO lấy hộ
            ItemDAO itemDAO = new ItemDAO();
            UserDAO userDAO = new UserDAO();

            while (rs.next()) {
                Auction auction = new Auction();
                auction.setId(rs.getString("id"));
                auction.setCurrentPrice(rs.getDouble("current_price"));
                auction.setStatus(AuctionStatus.valueOf(rs.getString("status")));

                // Chuyển Timestamp từ Database ngược lại thành LocalDateTime
                if (rs.getTimestamp("start_time") != null) {
                    auction.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
                }
                if (rs.getTimestamp("end_time") != null) {
                    auction.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
                }

                // --- Xử lý ghép nối Item ---
                String itemId = rs.getString("item_id");
                try {
                    Item item = itemDAO.findById(itemId);
                    auction.setItem(item);
                } catch (EntityNotFoundException e) {
                    System.err.println("Cảnh báo: Không tìm thấy Item gốc cho Auction " + auction.getId());
                }

                // --- Xử lý ghép nối Winner ---
                String winnerId = rs.getString("current_winner_id");
                if (winnerId != null) {
                    try {
                        User winner = userDAO.findById(winnerId);
                        auction.setCurrentWinner((Bidder) winner); // Ép kiểu sang Bidder
                    } catch (EntityNotFoundException e) {
                        System.err.println("Cảnh báo: Không tìm thấy Winner cho Auction " + auction.getId());
                    }
                }

                auctions.add(auction);
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi khi đọc danh sách Auction từ DB", e);
        }
        return auctions;
    }


    // 4. TÌM THEO ID (FIND BY ID)

    public Auction findById(String id) throws DataPersistenceException, EntityNotFoundException {
        return findAll().stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phiên đấu giá có ID: " + id));
    }


    // 5. XÓA (DELETE)

    public void delete(String id) throws DataPersistenceException, EntityNotFoundException {
        String sql = "DELETE FROM auctions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance();
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
    /**
     * Lấy danh sách tất cả các phiên đấu giá đang ở trạng thái OPEN.
     * Dùng để khôi phục bộ đếm thời gian khi Server khởi động lại.
     */
    public List<Auction> getOpenAuctions() {
        List<Auction> openAuctions = new ArrayList<>();
        // Truy vấn kết nối bảng auctions với bảng items (để lấy thông tin món đồ) và bảng users (nếu có người đang thắng)
        String sql = "SELECT a.*, i.name as item_name, i.description as item_desc, i.starting_price, i.item_type, " +
                "u.username as winner_username " +
                "FROM auctions a " +
                "JOIN items i ON a.item_id = i.id " +
                "LEFT JOIN users u ON a.current_winner_id = u.id " +
                "WHERE a.status = 'OPEN'";

        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Auction auction = new Auction();
                auction.setId(rs.getString("id"));
                auction.setCurrentPrice(rs.getDouble("current_price"));
                auction.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
                auction.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
                auction.setStatus(AuctionStatus.valueOf(rs.getString("status")));

                // 1. Lấy các giá trị cần thiết từ Database ra các biến trung gian
                String itemTypeStr = rs.getString("item_type");
                ItemType itemType = ItemType.valueOf(itemTypeStr);
                String itemId = rs.getString("item_id");
                String itemName = rs.getString("item_name");
                double startingPrice = rs.getDouble("starting_price");

                // 2. Gọi ItemFactory với 4 tham số theo đúng thiết kế của bạn
                Item item = ItemFactory.createItem(itemType, itemId, itemName, startingPrice);

                // 3. Vì description không có trong tham số của Factory, ta gọi hàm set riêng
                item.setDescription(rs.getString("item_desc"));

                auction.setItem(item);

                // Khôi phục thông tin người thắng hiện tại (nếu có)
                String winnerId = rs.getString("current_winner_id");
                if (winnerId != null) {
                    Bidder winner = new Bidder();
                    winner.setId(winnerId);
                    winner.setName(rs.getString("winner_username"));
                    auction.setCurrentWinner(winner);
                }

                openAuctions.add(auction);
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách phiên đấu giá đang mở: " + e.getMessage());
        }

        return openAuctions;
    }

}
