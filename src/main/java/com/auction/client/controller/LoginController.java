package com.auction.client.controller;

import com.auction.client.network.ServerConnection;
import com.auction.client.util.SessionManager;
import com.auction.shared.model.entity.User;
import com.auction.shared.protocol.Request;
import com.auction.shared.protocol.RequestType;
import com.auction.client.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/** Controller cho login.fxml */
public class LoginController {

    @FXML private TextField emailField;      // ô nhập email
    @FXML private PasswordField passwordField; // ô nhập password
    @FXML private Button signinButton;       // nút SIGN IN
    @FXML private Button signupButton;       // nút SIGN UP
    @FXML private Label errorLabel;          // label hiển thị lỗi

    @FXML
    public void initialize() {
        errorLabel.setText("");  // ẩn label lỗi khi mới mở
    }

    @FXML
    private void handleSignIn() {
        // Bước 1: Lấy text người dùng nhập
        String email = emailField.getText();
        String password = passwordField.getText();

        // Bước 2: Bỏ khoảng trắng thừa
        email = email.trim();
        password = password.trim();

        // Bước 3: Kiểm tra có bỏ trống không
        if (email.isEmpty()) {
            errorLabel.setText("Chưa nhập email!");
            return; // dừng lại, không chạy tiếp
        }

        if (password.isEmpty()) {
            errorLabel.setText("Chưa nhập mật khẩu!");
            return;
        }

        String[] credentials = {email, password};

        Request req = new Request(RequestType.LOGIN, credentials);

        ServerConnection.getInstance().sendRequestAsync(req, response -> {
            if (response.isSuccess()) {
                // Đăng nhập thành công -> Lưu User vào Session
                User loggedInUser = (User) response.getData();
                SessionManager.getInstance().setCurrentUser(loggedInUser);

                errorLabel.setStyle("-fx-text-fill: #00ff00;");
                errorLabel.setText("Đăng nhập thành công!");
                SceneManager.switchTo("/com/auction/fxml/auction/auction-list.fxml");
            } else {
                errorLabel.setStyle("-fx-text-fill: #ff6b6b;");
                errorLabel.setText(response.getMessage()); // Hiển thị lỗi "Sai mật khẩu" từ Server
            }
        });
    }

    @FXML
    private void handleRegisterLink() {
        SceneManager.switchTo("/com/auction/fxml/auth/register.fxml");
    }
}
