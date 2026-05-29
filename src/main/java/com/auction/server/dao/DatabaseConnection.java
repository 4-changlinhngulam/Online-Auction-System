package com.auction.server.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    private static String URL;
    private static String USER;
    private static String PASSWORD;

    private static DatabaseConnection instance;

    // Khối static: Sẽ tự động chạy 1 lần duy nhất khi class này được gọi đến
    static {
        String configFileName = "application.properties";
        // Nếu tồn tại file cấu hình test, ưu tiên sử dụng nó để kết nối tới database ảo
        if (DatabaseConnection.class.getClassLoader().getResource("application-test.properties") != null) {
            configFileName = "application-test.properties";
        }

        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream(configFileName)) {

            Properties prop = new Properties();

            if (input == null) {
                Logger.getLogger(DatabaseConnection.class.getName())
                      .log(Level.SEVERE, "LỖI NGHIÊM TRỌNG: Không tìm thấy file " + configFileName + "! "
                                       + "Vui lòng copy file application.properties.example, "
                                       + "đổi tên thành application.properties và điền mật khẩu.");
            } else {
                // Tải thông tin từ file properties vào bộ nhớ
                prop.load(input);

                URL = prop.getProperty("db.url");
                USER = prop.getProperty("db.username");
                PASSWORD = prop.getProperty("db.password");
            }

        } catch (IOException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, "Lỗi khi đọc file cấu hình Database: " + ex.getMessage(), ex);
        }
    }

    private DatabaseConnection() {}

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            LOGGER.info("Đã kết nối thành công tới Database qua cấu hình file!");
            return connection;
        } catch (ClassNotFoundException e) {
            throw new SQLException("Không tìm thấy MySQL Driver!", e);
        }
    }
}
