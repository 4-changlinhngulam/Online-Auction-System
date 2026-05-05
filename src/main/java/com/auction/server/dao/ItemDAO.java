package com.auction.server.dao;

import com.auction.server.service.ItemFactory;
import com.auction.shared.exception.DataPersistenceException;
import com.auction.shared.exception.EntityNotFoundException;
import com.auction.shared.model.entity.Art;
import com.auction.shared.model.entity.Electronics;
import com.auction.shared.model.entity.Item;
import com.auction.shared.model.entity.Vehicle;
import com.auction.shared.model.enums.ItemType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {


    // 1. THÊM MỚI SẢN PHẨM (SAVE)

    public void save(Item item) throws DataPersistenceException {
        if (item == null || item.getId() == null) {
            throw new IllegalArgumentException("Item và ID không được phép null");
        }

        String sql = "INSERT INTO items (id, name, description, starting_price, item_type) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getId());
            pstmt.setString(2, item.getName());
            pstmt.setString(3, item.getDescription());
            pstmt.setDouble(4, item.getStartingPrice());

            // Phân loại Item
            if (item instanceof Electronics) {
                pstmt.setString(5, "ELECTRONICS");
            } else if (item instanceof Vehicle) {
                pstmt.setString(5, "VEHICLE");
            } else {
                pstmt.setString(5, "ART");
            }

            pstmt.executeUpdate();
            System.out.println("Đã lưu Sản phẩm " + item.getName() + " lên Database!");

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("Sản phẩm với ID '" + item.getId() + "' đã tồn tại!");
        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi khi lưu Item vào Database", e);
        }
    }


    // 2. CẬP NHẬT SẢN PHẨM (UPDATE)

    public void update(Item item) throws DataPersistenceException, EntityNotFoundException {
        if (item == null || item.getId() == null) {
            throw new IllegalArgumentException("Item và ID không được phép null");
        }

        String sql = "UPDATE items SET name = ?, description = ?, starting_price = ?, item_type = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getDescription());
            pstmt.setDouble(3, item.getStartingPrice());

            if (item instanceof Electronics) pstmt.setString(4, "ELECTRONICS");
            else if (item instanceof Vehicle) pstmt.setString(4, "VEHICLE");
            else pstmt.setString(4, "ART");

            pstmt.setString(5, item.getId());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new EntityNotFoundException("Không tìm thấy Sản phẩm ID: " + item.getId() + " để cập nhật.");
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi khi cập nhật Item", e);
        }
    }


    // 3. TÌM THEO ID (FIND BY ID)

    public Item findById(String id) throws DataPersistenceException, EntityNotFoundException {
        String sql = "SELECT * FROM items WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToItem(rs);
                } else {
                    throw new EntityNotFoundException("Không tìm thấy Sản phẩm có ID: " + id);
                }
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi khi tìm Item theo ID", e);
        }
    }


    // 4. LẤY TẤT CẢ SẢN PHẨM (FIND ALL)

    public List<Item> findAll() throws DataPersistenceException {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi khi lấy danh sách Item", e);
        }
        return items;
    }

    // 5. XÓA SẢN PHẨM (DELETE)

    public void delete(String id) throws DataPersistenceException, EntityNotFoundException {
        String sql = "DELETE FROM items WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new EntityNotFoundException("Không tìm thấy Sản phẩm ID: " + id + " để xóa.");
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi khi xóa Item", e);
        }
    }

    // 7. TÌM BẰNG TỪ KHÓA
        
    public List<Item> searchByName(String keyword) {
        List<Item> resultList = new ArrayList<>();
        // Sử dụng LIKE để tìm kiếm chứa từ khóa, bất kể chữ hoa chữ thường
        String sql = "SELECT * FROM items WHERE name LIKE ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Thêm dấu % vào 2 đầu từ khóa để tìm kiếm chuỗi con
            pstmt.setString(1, "%" + keyword + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Tái sử dụng ItemFactory
                    ItemType type = ItemType.valueOf(rs.getString("item_type"));
                    Item item = ItemFactory.createItem(
                            type,
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getDouble("starting_price")
                    );
                    item.setDescription(rs.getString("description"));

                    resultList.add(item);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm kiếm Item theo tên: " + e.getMessage());
        }
        return resultList;
    }

    // --- HÀM HỖ TRỢ: CHUYỂN ĐỔI DỮ LIỆU TỪ DB SANG JAVA OBJECT ---
    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        String type = rs.getString("item_type");
        Item item;

        // Khởi tạo Object tương ứng với Item Type
        if ("ELECTRONICS".equals(type)) {
            item = new Electronics();
            ((Electronics) item).setWarrantyMonths(rs.getInt("warranty_months"));
        } else if ("VEHICLE".equals(type)) {
            item = new Vehicle();
            ((Vehicle) item).setMileage(rs.getLong("mileage"));
        } else {
            item = new Art();
        }

        // Đổ dữ liệu chung vào
        item.setId(rs.getString("id"));
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setStartingPrice(rs.getDouble("starting_price"));

        return item;
    }
}