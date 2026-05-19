package com.auction.server.dao;

import com.auction.shared.model.entity.Bidder;
import com.auction.shared.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOIntegrationTest {

    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws Exception {
        userDAO = new UserDAO();

        // Mở kết nối thật (Tới H2 nhờ file properties ở src/test)
        Connection conn = DatabaseConnection.getInstance().getConnection();
        Statement stmt = conn.createStatement();

        // Vì H2 chạy trên RAM nên khởi đầu nó hoàn toàn trống rỗng.
        // Ta cần dùng SQL tạo bảng users trước khi test và dọn sạch dữ liệu cũ
        stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "username VARCHAR(50) UNIQUE, " +
                "password VARCHAR(255), " +
                "email VARCHAR(100), " +
                "role VARCHAR(20))");
        stmt.execute("TRUNCATE TABLE users");
    }

    @Test
    @DisplayName("Test luồng thật: Lưu User và Tìm lại bằng ID")
    void testSaveAndFindById_RealDB() throws Exception {
        // 1. Tạo đối tượng User
        User user = new Bidder();
        user.setId("USR_REAL_01");
        user.setName("realuser");
        user.setPassword("realpass");
        user.setEmail("real@gmail.com");

        // 2. Kích hoạt hàm save (Lệnh INSERT SQL sẽ thực thi thật)
        assertDoesNotThrow(() -> userDAO.save(user));

        // 3. Kích hoạt hàm find (Lệnh SELECT SQL sẽ thực thi thật)
        User fetchedUser = userDAO.findById("USR_REAL_01");

        // 4. Kiểm chứng dữ liệu trả về từ DB
        assertNotNull(fetchedUser);
        assertEquals("USR_REAL_01", fetchedUser.getId());
        assertEquals("realuser", fetchedUser.getName()); // mapResultSetToUser dùng getName()
        assertEquals("real@gmail.com", fetchedUser.getEmail());
    }

    @Test
    @DisplayName("Test luồng thật: Cập nhật User")
    void testUpdate_RealDB() throws Exception {
        // Lưu user ban đầu
        User user = new Bidder("update_user", "oldpass", "old@gmail.com");
        user.setId("USR_REAL_02");
        userDAO.save(user);

        // Chỉnh sửa thông tin và update
        user.setPassword("newpass");
        user.setEmail("new@gmail.com");
        userDAO.update(user);

        // Truy vấn lại từ DB để xem đã đổi chưa
        User updatedUser = userDAO.findById("USR_REAL_02");
        assertEquals("newpass", updatedUser.getPassword());
        assertEquals("new@gmail.com", updatedUser.getEmail());
    }
}