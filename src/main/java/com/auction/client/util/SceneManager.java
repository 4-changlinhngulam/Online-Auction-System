package com.auction.client.util;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
/** Tiện ích điều hướng màn hình trong JavaFX – tránh lặp code loadFXML. */
public class SceneManager {
    private static Stage primaryStage;
    public static void setPrimaryStage(Stage stage) { primaryStage = stage; }
    public static void switchTo(String fxmlPath) throws Exception {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
        primaryStage.setScene(new Scene(loader.load()));
    }
    @SuppressWarnings("unchecked")
    public static <T> T getController(String fxmlPath) throws Exception {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
        loader.load();
        return loader.getController();
    }
}
