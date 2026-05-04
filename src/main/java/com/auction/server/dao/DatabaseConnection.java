package com.auction.server.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static String URL;
    private static String USER;
    private static String PASSWORD;

    private static Connection connection;

    // Khối static: Sẽ tự động chạy 1 lần duy nhất khi class này được gọi đến
    static {
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("application.properties")) {

            Properties prop = new Properties();

            if (input == null) {
                System.err.println("LỖI NGHIÊM TRỌNG: Không tìm thấy file application.properties trong thư mục resources!");
                System.err.println("Vui lòng copy file application.properties.example, đổi tên và điền mật khẩu.");
            } else {
                // Tải thông tin từ file properties vào bộ nhớ
                prop.load(input);

                URL = prop.getProperty("db.url");
                USER = prop.getProperty("db.username");
                PASSWORD = prop.getProperty("db.password");
            }

        } catch (Exception ex) {
            System.err.println("Lỗi khi đọc file cấu hình Database: " + ex.getMessage());
        }
    }

    private DatabaseConnection() {}

    public static synchronized Connection getInstance() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                // Sử dụng các biến đã được đọc từ file
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Đã kết nối thành công tới Database qua cấu hình file!");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Không tìm thấy MySQL Driver!", e);
            }
        }
        return connection;
    }
}