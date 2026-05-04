package com.auction.server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Khai báo các hằng số kết nối (Thay đổi password cho đúng với máy của bạn)
    private static final String URL = "jdbc:mysql://mysql-cdf247b-nguyenvietngocduy2003-5af7.l.aivencloud.com:24276/defaultdb?sslMode=REQUIRED";
    private static final String USER = "avnadmin";
    private static final String PASSWORD = "AVNS_hMMKooFWxtwpdgGJVHT"; // <-- Điền mật khẩu MySQL của bạn vào đây

    // Instance duy nhất (Singleton)
    private static Connection connection;

    // Constructor private để không ai được new DatabaseConnection()
    private DatabaseConnection() {}

    // Hàm lấy kết nối (Có khóa đồng bộ synchronized để an toàn khi đa luồng)
    public static synchronized Connection getInstance() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Tải Driver (Bắt buộc với một số phiên bản Java cũ, Java mới có thể tự nhận)
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Đã kết nối thành công tới cơ sở dữ liệu MySQL!");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Không tìm thấy MySQL Driver!", e);
            }
        }
        return connection;
    }
}