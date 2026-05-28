package com.auction.client.controller;

import com.auction.client.network.ServerConnection;
import com.auction.client.util.SceneManager;
import com.auction.client.util.SessionManager;
import com.auction.shared.model.entity.User;
import com.auction.shared.protocol.Request;
import com.auction.shared.protocol.RequestType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ProfileController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            usernameField.setText(currentUser.getUsername());
            emailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        }
    }

    @FXML
    private void handleUpdate() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || email.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: #ff6b6b;");
            messageLabel.setText("Vui lòng điền đủ Tên và Email!");
            return;
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        currentUser.setName(username);
        currentUser.setEmail(email);
        if (!password.isEmpty()) {
            currentUser.setPassword(password);
        } else {
            currentUser.setPassword(""); // Server sẽ giữ nguyên mật khẩu cũ nếu empty
        }

        Request req = new Request(RequestType.UPDATE_USER_PROFILE, currentUser);
        ServerConnection.getInstance().sendRequestAsync(req, res -> {
            Platform.runLater(() -> {
                if (res != null && res.isSuccess()) {
                    messageLabel.setStyle("-fx-text-fill: #00ff00;");
                    messageLabel.setText("Cập nhật thông tin thành công!");
                    passwordField.clear();
                    
                    // Cập nhật lại Session với dữ liệu mới từ Server
                    if (res.getData() instanceof User) {
                        SessionManager.getInstance().setCurrentUser((User) res.getData());
                    }
                } else {
                    messageLabel.setStyle("-fx-text-fill: #ff6b6b;");
                    messageLabel.setText("Lỗi: " + (res != null ? res.getMessage() : "Không phản hồi"));
                }
            });
        });
    }

    @FXML
    private void goBack() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            if ("ADMIN".equals(currentUser.getRole())) {
                SceneManager.switchTo("/com/auction/fxml/admin/admin-auctions.fxml");
            } else {
                SceneManager.switchTo("/com/auction/fxml/auction/auction-list.fxml");
            }
        } else {
            SceneManager.switchTo("/com/auction/fxml/auth/login.fxml");
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().setCurrentUser(null);
        SceneManager.switchTo("/com/auction/fxml/auth/login.fxml");
    }
}
