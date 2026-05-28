package com.auction.client;

import com.auction.client.network.ServerConnection;
import com.auction.client.util.SceneManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.util.logging.Logger;

public class AuctionClientApp {
    private static final Logger LOGGER = Logger.getLogger(AuctionClientApp.class.getName());

    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }

    public static class MainApp extends Application {
        @Override
        public void start(Stage primaryStage) throws Exception {
            // Bước 1: Kết nối tới Server
            try {
                ServerConnection.getInstance().connect();
                LOGGER.info("Đã kết nối tới Server thành công!");
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi kết nối");
                alert.setHeaderText("Không thể kết nối tới Server");
                alert.setContentText("Vui lòng kiểm tra Server đang chạy trên port 9999.\nChi tiết: " + e.getMessage());
                alert.showAndWait();
                // Vẫn tiếp tục mở app (để user thấy giao diện)
            }

            // Bước 2: Đăng ký stage vào SceneManager
            SceneManager.setPrimaryStage(primaryStage);

            // Bước 3: Load màn hình Login đầu tiên
            Parent root = FXMLLoader.load(
                    getClass().getResource(
                            "/com/auction/fxml/auth/login.fxml"
                    )
            );

            // Bước 4: Tạo scene và hiển thị
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Hệ thống Đấu Giá Trực Tuyến");
            primaryStage.setResizable(false); // cố định kích thước
            primaryStage.show();
        }
    }
}
