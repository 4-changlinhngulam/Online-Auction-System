package com.auction.client.controller;

import com.auction.client.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        // Thêm các role vào ComboBox
        roleComboBox.getItems().addAll("BIDDER", "SELLER");
        roleComboBox.setValue("BIDDER"); // mặc định
        errorLabel.setText("");
    }

    @FXML
    private void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String role = roleComboBox.getValue();

        // Validate bỏ trống
        if (fullName.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {
            errorLabel.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // Validate mật khẩu khớp
        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Mật khẩu không khớp!");
            return;
        }

        // Validate email
        if (!email.contains("@")) {
            errorLabel.setText("Email không hợp lệ!");
            return;
        }

        // --- MOCK MODE ---
        errorLabel.setStyle("-fx-text-fill: #00ff00;");
        errorLabel.setText("Đăng ký thành công!");

        // TODO: Gửi Request(REGISTER) lên Server
        // TODO: Chuyển về login sau khi đăng ký thành công
    }

    @FXML
    private void handleBackToLogin() {
        SceneManager.switchTo(
                "/com/auction/fxml/auth/login.fxml"
        );
    }
}