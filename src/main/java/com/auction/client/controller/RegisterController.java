package com.auction.client.controller;
import javafx.fxml.FXML;
import javafx.scene.control.*;
/** Controller cho register.fxml */
public class RegisterController {
    @FXML private TextField usernameField, emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;
    @FXML
    private void handleRegister() {
        // TODO: Validate → Request(REGISTER) → phản hồi
    }
}
