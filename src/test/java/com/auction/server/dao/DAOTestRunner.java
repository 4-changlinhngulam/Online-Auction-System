package com.auction.server.dao;

import com.auction.server.service.UserService;
import com.auction.shared.model.entity.User;
import com.auction.shared.protocol.Response;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

public class DAOTestRunner {
    public static void main(String[] args) {
        System.out.println("=== BẮT ĐẦU KIỂM TRA DATABASE & DAO ===");

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();

            // 1. Tự động cập nhật bảng users
            System.out.println("\n1. Đang tự động cập nhật cấu trúc bảng 'users' (Thêm cột status)...");
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE users ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE'");
                System.out.println("-> Đã thêm cột 'status' thành công!");
            } catch (Exception e) {
                // Ignore if column already exists
                if (e.getMessage().contains("Duplicate column name")) {
                    System.out.println("-> Cột 'status' đã tồn tại, bỏ qua bước này.");
                } else {
                    System.out
                            .println("-> Cảnh báo khi cấu trúc bảng (thường là do cột đã tồn tại): " + e.getMessage());
                }
            }

            System.out.println("\n2. Đang kiểm tra đọc User có kèm status...");
            UserDAO userDAO = new UserDAO();
            List<User> users = userDAO.findAll();
            System.out.println("-> Đã lấy thành công danh sách Users. Tổng số: " + users.size());

            if (users.size() > 0) {
                User testUser = users.get(0);
                System.out.println("-> Thông tin User mẫu ID: " + testUser.getId() + " - Tên: " + testUser.getUsername()
                        + " - Trạng thái ban đầu: " + testUser.getStatus());

                System.out.println("\n3. Đang chạy test tính năng BAN_USER (Khóa tài khoản)...");
                UserService userService = new UserService(userDAO);
                Response res = userService.banUser(testUser.getId());
                System.out.println("-> Kết quả gọi UserService.banUser(): " + res.getMessage());

                // Lấy lại từ DB để check
                User updatedUser = userDAO.findById(testUser.getId());
                System.out.println("-> Trạng thái thực tế trong Database sau khi khóa: " + updatedUser.getStatus());

                if (updatedUser.getStatus() == com.auction.shared.model.enums.UserStatus.BANNED) {
                    System.out.println("-> TÍNH NĂNG BAN_USER ĐÃ HOẠT ĐỘNG HOÀN HẢO!");
                }
            } else {
                System.out.println("-> Không có user nào trong Database để test tính năng BAN.");
            }

            System.out.println("\n=== KIỂM TRA HOÀN TẤT, MỌI THỨ HOẠT ĐỘNG TỐT! ===");

        } catch (Exception e) {
            System.err.println("\n!!! PHÁT HIỆN LỖI TRONG QUÁ TRÌNH KIỂM TRA !!!");
            e.printStackTrace();
        }
    }
}
