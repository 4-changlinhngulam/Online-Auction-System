package com.auction.client;

import com.auction.client.controller.AuctionDetailController;
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

                // Đăng ký nhận Push Notification toàn cục
                ServerConnection.getInstance().setPushNotificationListener(res -> {
                    if ("NOTIFICATION_NEW_BID".equals(res.getMessage())) {
                        Object[] data = (Object[]) res.getData();
                        com.auction.shared.model.entity.Item item = (com.auction.shared.model.entity.Item) data[0];
                        double newPrice = (Double) data[1];

                        String title = "Giá mới: " + item.getName();
                        String message = String.format("Có người vừa đặt lên: %,.0f VND", newPrice);

                        com.auction.client.util.NotificationUtil.showPushNotification(title, message);

                        // Cập nhật giao diện chi tiết phiên đấu giá (nếu người dùng đang mở trang đó)
                        AuctionDetailController controller = AuctionDetailController.getInstance();
                        if (controller != null) {
                            String lastBidderId = (String) data[2];
                            java.time.LocalDateTime newEndTime = (java.time.LocalDateTime) data[3];
                            controller.onBidUpdate(item, newPrice, lastBidderId, newEndTime);
                        }
                    }
                });
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
///            primaryStage.setResizable(false); // cố định kích thước
            primaryStage.show();
        }
    }
}
