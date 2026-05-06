package com.auction.server.dao;

import com.auction.shared.exception.DataPersistenceException;
import com.auction.shared.exception.EntityNotFoundException;
import com.auction.shared.model.entity.Admin;
import com.auction.shared.model.entity.Bidder;
import com.auction.shared.model.entity.Seller;
import com.auction.shared.model.entity.User;
import com.auction.shared.model.enums.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {


    // 1. THÊM MỚI USER (SAVE)

    public void save(User user) throws DataPersistenceException {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User và ID không được phép null");
        }

        String sql = "INSERT INTO users (id, username, password, email, role) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getEmail());

            // Xác định Role dựa trên Class con
            if (user instanceof Admin) {
                pstmt.setString(5, UserRole.ADMIN.name());
            } else if (user instanceof Seller) {
                pstmt.setString(5, UserRole.SELLER.name());
            } else {
                pstmt.setString(5, UserRole.BIDDER.name());
            }

            pstmt.executeUpdate();
            System.out.println("Đã lưu User " + user.getUsername() + " lên Database!");

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("Username hoặc ID đã tồn tại trong hệ thống!");
        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi khi lưu User vào Database", e);
        }
    }

    // 2. CẬP NHẬT USER (UPDATE)

    public void update(User user) throws DataPersistenceException, EntityNotFoundException {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User và ID không được phép null");
        }

        String sql = "UPDATE users SET username = ?, password = ?, email = ?, role = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());

            if (user instanceof Admin) pstmt.setString(4, UserRole.ADMIN.name());
            else if (user instanceof Seller) pstmt.setString(4, UserRole.SELLER.name());
            else pstmt.setString(4, UserRole.BIDDER.name());

            pstmt.setString(5, user.getId());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new EntityNotFoundException("Không tìm thấy User có ID: " + user.getId() + " để cập nhật.");
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi khi cập nhật User", e);
        }
    }

    // 3. TÌM THEO ID (FIND BY ID)

    public User findById(String id) throws DataPersistenceException, EntityNotFoundException {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                } else {
                    throw new EntityNotFoundException("Không tìm thấy User có ID: " + id);
                }
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi khi tìm User theo ID", e);
        }
    }


    // 4. LẤY TOÀN BỘ USER (FIND ALL)

    public List<User> findAll() throws DataPersistenceException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi khi lấy danh sách User", e);
        }
        return users;
    }


    // 5. XÓA USER BẰNG ID (DELETE by ID)

    public void deleteByID(String id) throws DataPersistenceException, EntityNotFoundException {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new EntityNotFoundException("Không tìm thấy User ID: " + id + " để xóa.");
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi khi xóa User", e);
        }
    }

    // 6. TÌM USER THEO TÊN (FIND by USERNAME)
    public User findByUsername(String username) throws DataPersistenceException {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi khi tìm User theo Username", e);
        }
    }
    // --- HÀM HỖ TRỢ: CHUYỂN ĐỔI DỮ LIỆU TỪ DB SANG JAVA OBJECT ---
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        User user;

        // Khởi tạo Object tương ứng với Role
        if ("ADMIN".equals(role)) {
            user = new Admin();
        } else if ("SELLER".equals(role)) {
            user = new Seller();
        } else {
            user = new Bidder();
        }

        // Đổ dữ liệu chung vào
        user.setId(rs.getString("id"));
        user.setName(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));

        return user;
    }
}