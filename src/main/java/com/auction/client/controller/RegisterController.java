package com.auction.client.controller;
import com.auction.client.network.ServerConnection;
import com.auction.shared.model.entity.Bidder;
import com.auction.shared.model.entity.Seller;
import com.auction.shared.model.entity.User;
import com.auction.shared.protocol.Request;
import com.auction.shared.protocol.RequestType;
import com.auction.client.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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
        // 1. Tạo đối tượng User tương ứng với Role
        User newUser;
        if ("SELLER".equals(role)) {
            newUser = new Seller(fullName, password, email);
        } else {
            newUser = new Bidder(fullName, password, email);
        }

        // 2. Tạo Request đóng gói dữ liệu
        Request req = new Request(RequestType.REGISTER, newUser);

        // 3. Gửi bất đồng bộ lên Server
        ServerConnection.getInstance().sendRequestAsync(req, response -> {
            if (response.isSuccess()) {
                errorLabel.setStyle("-fx-text-fill: #00ff00;");
                errorLabel.setText("Đăng ký thành công! Đang chuyển về đăng nhập...");

                // Đợi 1 chút rồi chuyển về Login
                new Thread(() -> {
                    try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                    javafx.application.Platform.runLater(() -> handleBackToLogin());
                }).start();
            } else {
                errorLabel.setStyle("-fx-text-fill: #ff6b6b;");
                errorLabel.setText(response.getMessage()); // Báo lỗi (VD: Trùng username)
            }
        });



    }

    @FXML
    private void handleBackToLogin() {
        SceneManager.switchTo(
                "/com/auction/fxml/auth/login.fxml"
        );
    }
}
