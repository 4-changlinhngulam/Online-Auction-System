package com.auction.client.controller;

import com.auction.client.util.SceneManager;
import com.auction.shared.model.entity.Item;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;

public class ProductManageController {

    @FXML private TableView<Item> productTable;
    @FXML private TableColumn<Item, String> colProductName;
    @FXML private TableColumn<Item, String> colCategory;
    @FXML private TableColumn<Item, String> colStartPrice;
    @FXML private Label messageLabel;

    private List<Item> myProducts = new ArrayList<>();

    @FXML
    public void initialize() {
        colProductName.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getName()
                )
        );
        colCategory.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getClass().getSimpleName()
                )
        );
        colStartPrice.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%,.0f VND",
                                data.getValue().getStartingPrice())
                )
        );

        loadProducts();
    }

    private void loadProducts() {
        // --- MOCK MODE ---
        myProducts = new ArrayList<>();
        productTable.setItems(
                FXCollections.observableArrayList(myProducts)
        );
        messageLabel.setText("Chưa có sản phẩm nào.");

        // --- REAL MODE ---
        // Request req = new Request(RequestType.GET_ALL_ITEMS, null);
        // Response res = ServerConnection.getInstance()
        //                                .sendRequest(req);
        // myProducts = (List<Item>) res.getData();
    }

    @FXML
    private void handleAddProduct() {
        SceneManager.switchTo(
                "/com/auction/fxml/product/product-form.fxml"
        );
    }

    @FXML
    private void handleEdit() {
        Item selected = productTable.getSelectionModel()
                .getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Vui lòng chọn sản phẩm!");
            return;
        }
        SceneManager.switchTo(
                "/com/auction/fxml/product/product-form.fxml"
        );
    }

    @FXML
    private void handleDelete() {
        Item selected = productTable.getSelectionModel()
                .getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Vui lòng chọn sản phẩm!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setContentText(
                "Bạn có chắc muốn xóa sản phẩm này?"
        );
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                myProducts.remove(selected);
                productTable.setItems(
                        FXCollections.observableArrayList(myProducts)
                );
                messageLabel.setStyle("-fx-text-fill: #00ff00;");
                messageLabel.setText("Xóa thành công!");
                // TODO: Request(DELETE_ITEM)
            }
        });
    }

    @FXML
    private void handleCreateAuction() {
        Item selected = productTable.getSelectionModel()
                .getSelectedItem();
        if (selected == null) {
            messageLabel.setText(
                    "Vui lòng chọn sản phẩm để tạo phiên!"
            );
            return;
        }
        // TODO: Request(CREATE_AUCTION)
        messageLabel.setStyle("-fx-text-fill: #00ff00;");
        messageLabel.setText("Tạo phiên đấu giá thành công!");
    }

    @FXML
    private void handleBack() {
        SceneManager.switchTo(
                "/com/auction/fxml/auction/auction-list.fxml"
        );
    }
}