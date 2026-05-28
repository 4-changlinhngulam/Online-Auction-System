package com.auction.client.controller;

import com.auction.client.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import com.auction.shared.model.entity.Item;
import com.auction.shared.model.entity.Art;
import com.auction.shared.model.entity.Electronics;
import com.auction.shared.model.entity.Vehicle;
import com.auction.shared.protocol.Request;
import com.auction.shared.protocol.RequestType;
import com.auction.client.network.ServerConnection;

public class ProductFormController {

    @FXML
    private TextField nameField;
    @FXML
    private TextArea descField;
    @FXML
    private ComboBox<String> categoryCombo;
    @FXML
    private TextField priceField;
    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        categoryCombo.setItems(FXCollections.observableArrayList(
                "ELECTRONICS", "ART", "VEHICLE"));
        categoryCombo.setValue("ELECTRONICS");
        errorLabel.setText("");
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        String desc = descField.getText().trim();
        String category = categoryCombo.getValue();
        String priceStr = priceField.getText().trim();

        // Validate
        if (name.isEmpty() || desc.isEmpty() || priceStr.isEmpty()) {
            errorLabel.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            if (price <= 0) {
                errorLabel.setText("Giá phải lớn hơn 0!");
                return;
            }

            // --- REAL MODE ---
            Item item = null;
            switch (category) {
                case "ART":
                    item = new Art();
                    break;
                case "ELECTRONICS":
                    item = new Electronics();
                    break;
                case "VEHICLE":
                    item = new Vehicle();
                    break;
                default:
                    errorLabel.setText("Danh mục không hợp lệ!");
                    return;
            }

            item.setName(name);
            item.setDescription(desc);
            item.setStartingPrice(price);
            
            // Gán owner là user hiện tại (có thể null nếu Server xử lý, nhưng gửi kèm ID qua session thì tốt hơn)
            // if (SessionManager.getInstance().getCurrentUser() != null) {
            //     item.setOwner(SessionManager.getInstance().getCurrentUser());
            // }

            ServerConnection.getInstance().sendRequestAsync(
                new Request(RequestType.CREATE_ITEM, item),
                response -> {
                    Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Lưu sản phẩm thành công!");
                            SceneManager.switchTo("/com/auction/fxml/product/product-manage.fxml");
                        } else {
                            errorLabel.setText(response.getMessage());
                        }
                    });
                }
            );

        } catch (NumberFormatException e) {
            errorLabel.setText("Giá không hợp lệ!");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        SceneManager.switchTo(
                "/com/auction/fxml/product/product-manage.fxml");
    }
}
