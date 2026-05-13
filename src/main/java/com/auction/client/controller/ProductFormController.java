package com.auction.client.controller;

import com.auction.client.util.SceneManager;
import com.auction.shared.model.entity.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ProductFormController {

    @FXML private TextField nameField;
    @FXML private TextArea descField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField priceField;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        categoryCombo.setItems(FXCollections.observableArrayList(
                "ELECTRONICS", "ART", "VEHICLE"
        ));
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

            // --- MOCK MODE ---
            errorLabel.setStyle("-fx-text-fill: #00ff00;");
            errorLabel.setText("Lưu sản phẩm thành công!");

            // TODO: ItemFactory.createItem(category, params)
            // TODO: Request(CREATE_ITEM)
            // TODO: Quay về product-manage sau khi lưu

        } catch (NumberFormatException e) {
            errorLabel.setText("Giá không hợp lệ!");
        }
    }

    @FXML
    private void handleBack() {
        SceneManager.switchTo(
                "/com/auction/fxml/product/product-manage.fxml"
        );
    }
}