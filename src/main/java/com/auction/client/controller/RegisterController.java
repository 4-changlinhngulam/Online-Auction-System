package com.auction.client.controller;

import com.auction.client.network.ServerConnection;
import com.auction.client.util.SceneManager;
import com.auction.shared.model.entity.Bidder;
import com.auction.shared.model.entity.Seller;
import com.auction.shared.model.entity.User;
import com.auction.shared.model.enums.UserStatus;
import com.auction.shared.protocol.Request;
import com.auction.shared.protocol.RequestType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.UUID;

public class RegisterController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;
    @FXML private Button registerButton;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("BIDDER", "SELLER");
        roleComboBox.setValue("BIDDER");
        errorLabel.setText("");
    }

    @FXML
    private void handleRegister() {
        String fullName        = fullNameField.getText().trim();
        String email           = emailField.getText().trim();
        String password        = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String role            = roleComboBox.getValue();

        // --- Validation ---
        if (fullName.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin!");
            return;
        }
        if (!email.contains("@")) {
            showError("Email không hợp lệ!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Mật khẩu không khớp!");
            return;
        }
        if (password.length() < 6) {
            showError("Mật khẩu phải có ít nhất 6 ký tự!");
            return;
        }

        // --- Tạo đối tượng User tương ứng với role ---
        User newUser;
        if ("SELLER".equals(role)) {
            newUser = new Seller(fullName, password, email);
        } else {
            newUser = new Bidder(fullName, password, email);
        }
        // Gán ID duy nhất (server cũng có thể tự sinh, nhưng để an toàn sinh sẵn)
        newUser.setId(UUID.randomUUID().toString());
        newUser.setStatus(UserStatus.ACTIVE);

        // Vô hiệu hóa nút để tránh click nhiều lần
        registerButton.setDisable(true);
        errorLabel.setStyle("-fx-text-fill: #aaaaaa;");
        errorLabel.setText("Đang xử lý...");

        // --- Gửi Request lên Server ---
        Request request = new Request(RequestType.REGISTER, newUser);

        ServerConnection.getInstance().sendRequestAsync(request, response -> {
            registerButton.setDisable(false);

            if (response.isSuccess()) {
                errorLabel.setStyle("-fx-text-fill: #00ff00;");
                errorLabel.setText("Đăng ký thành công! Đang chuyển về trang đăng nhập...");

                // Chờ 1 giây rồi chuyển về login
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                        // ignored
                    }
                    javafx.application.Platform.runLater(() ->
                            SceneManager.switchTo("/com/auction/fxml/auth/login.fxml")
                    );
                }).start();
            } else {
                showError(response.getMessage());
            }
        });
    }

    @FXML
    private void handleBackToLogin() {
        SceneManager.switchTo("/com/auction/fxml/auth/login.fxml");
    }

    private void showError(String msg) {
        errorLabel.setStyle("-fx-text-fill: #ff6b6b;");
        errorLabel.setText(msg);
    }
}
