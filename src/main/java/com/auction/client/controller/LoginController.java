package com.auction.client.controller;
import javafx.fxml.FXML;
import javafx.scene.control.*;
/** Controller cho login.fxml */
public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML
    private void handleLogin() {
        // TODO: Gửi Request(LOGIN) → nhận Response → navigate hoặc hiện lỗi
    }
    @FXML
    private void handleRegisterLink() {
        // TODO: SceneManager.switchTo(".../auth/register.fxml")
    }
}
