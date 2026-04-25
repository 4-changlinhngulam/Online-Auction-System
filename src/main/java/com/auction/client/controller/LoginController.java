package com.auction.client.controller;
import com.auction.client.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

        // Bước 4: Kiểm tra đúng/sai (tạm thời dùng mock)
        if (email.equals("admin@gmail.com")
                && password.equals("123")) {
            errorLabel.setStyle("-fx-text-fill: #00ff00;");
            errorLabel.setText("Đăng nhập thành công!");
        } else {
            errorLabel.setStyle("-fx-text-fill: #ff6b6b;");
            errorLabel.setText("Sai email hoặc mật khẩu!");
        }
    }
    @FXML
    private void handleRegisterLink() {
//        SceneManager.switchTo(
//                "/com/auction/fxml/auth/register.fxml"
//        );
    }
}
