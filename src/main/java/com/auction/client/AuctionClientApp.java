package com.auction.client;

import com.auction.client.util.SceneManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AuctionClientApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Bước 1: Đăng ký stage vào SceneManager
        SceneManager.setPrimaryStage(primaryStage);

        // Bước 2: Load màn hình Login đầu tiên
        Parent root = FXMLLoader.load(
                getClass().getResource(
                        "/com/auction/fxml/auth/login.fxml"
                )
        );

        // Bước 3: Tạo scene và hiển thị
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Hệ thống Đấu Giá Trực Tuyến");
        primaryStage.setResizable(false); // cố định kích thước
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}