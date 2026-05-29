package com.auction.client.controller;

import com.auction.client.network.ServerConnection;
import com.auction.client.util.SceneManager;
import com.auction.client.util.SessionManager;
import com.auction.shared.model.entity.User;
import com.auction.shared.protocol.Request;
import com.auction.shared.protocol.RequestType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/** Controller cho login.fxml */
public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button signinButton;
    @FXML private Button signupButton;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        errorLabel.setText("");
    }

    @FXML
    private void handleSignIn() {
        String username = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty()) {
            showError("Chưa nhập tên đăng nhập!");
            return;
        }
        if (password.isEmpty()) {
            showError("Chưa nhập mật khẩu!");
            return;
        }

        // Vô hiệu hóa nút để tránh click nhiều lần
        signinButton.setDisable(true);
        errorLabel.setText("Đang đăng nhập...");
        errorLabel.setStyle("-fx-text-fill: #aaaaaa;");

        // Tạo payload: mảng [username, password]
        String[] credentials = {username, password};
        Request request = new Request(RequestType.LOGIN, credentials);

        // Gửi bất đồng bộ để không đơ UI
        ServerConnection.getInstance().sendRequestAsync(request, response -> {
            signinButton.setDisable(false);

            if (response.isSuccess()) {
                // Lưu thông tin user vào SessionManager
                User loggedInUser = (User) response.getData();
                SessionManager.getInstance().setCurrentUser(loggedInUser);

                errorLabel.setStyle("-fx-text-fill: #00ff00;");
                errorLabel.setText("Đăng nhập thành công!");

                // Đăng ký nhận notification
                ServerConnection.getInstance().sendRequestAsync(new Request(RequestType.SUBSCRIBE_AUCTION, null), subRes -> {});

                // Chuyển màn hình tuỳ theo role
                String role = loggedInUser.getRole();
                if ("ADMIN".equals(role)) {
                    SceneManager.switchTo("/com/auction/fxml/admin/admin-auctions.fxml");
                } else {
                    SceneManager.switchTo("/com/auction/fxml/auction/auction-list.fxml");
                }
            } else {
                showError(response.getMessage());
            }
        });
    }

    @FXML
    private void handleRegisterLink() {
        SceneManager.switchTo("/com/auction/fxml/auth/register.fxml");
    }

    private void showError(String msg) {
        errorLabel.setStyle("-fx-text-fill: #ff6b6b;");
        errorLabel.setText(msg);
    }
}
