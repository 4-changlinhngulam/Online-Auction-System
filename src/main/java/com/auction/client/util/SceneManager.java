package com.auction.client.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SceneManager {

    private static final Logger LOGGER = Logger.getLogger(SceneManager.class.getName());

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void switchTo(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(
                    SceneManager.class.getResource(fxmlPath)
            );
            primaryStage.setScene(new Scene(root));
            // Thêm dòng này để cho phép dùng chuột kéo giãn cửa sổ
            primaryStage.setResizable(true);
            primaryStage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi chuyển màn hình: " + e.getMessage(), e);
        }
    }
}
