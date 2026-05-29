package com.auction.client.controller;

import com.auction.client.util.SceneManager;
import com.auction.shared.model.entity.Item;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.application.Platform;
import com.auction.shared.protocol.Request;
import com.auction.shared.protocol.RequestType;
import com.auction.client.network.ServerConnection;
import com.auction.shared.model.entity.Auction;
import java.time.LocalDateTime;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

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
        try {
            com.auction.shared.protocol.Request req = new com.auction.shared.protocol.Request(
                com.auction.shared.protocol.RequestType.GET_MY_ITEMS, null);
            com.auction.client.network.ServerConnection.getInstance().sendRequestAsync(req, res -> {
                if (res != null && res.isSuccess() && res.getData() instanceof java.util.List) {
                    myProducts = (java.util.List<com.auction.shared.model.entity.Item>) res.getData();
                    productTable.setItems(javafx.collections.FXCollections.observableArrayList(myProducts));
                    if (myProducts.isEmpty()) {
                        messageLabel.setText("Chưa có sản phẩm nào.");
                    } else {
                        messageLabel.setText("");
                    }
                } else {
                    messageLabel.setText("Không thể lấy dữ liệu sản phẩm.");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddProduct() {
        ProductFormController.editingItem = null;
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
        ProductFormController.editingItem = selected;
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
                ServerConnection.getInstance().sendRequestAsync(
                    new Request(RequestType.DELETE_ITEM, selected.getId()),
                    response -> {
                        Platform.runLater(() -> {
                            if (response.isSuccess()) {
                                myProducts.remove(selected);
                                productTable.setItems(FXCollections.observableArrayList(myProducts));
                                messageLabel.setStyle("-fx-text-fill: #00ff00;");
                                messageLabel.setText("Xóa thành công!");
                            } else {
                                messageLabel.setStyle("-fx-text-fill: #ff0000;");
                                messageLabel.setText("Xóa thất bại: " + response.getMessage());
                            }
                        });
                    }
                );
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
        Auction newAuction = new Auction();
        newAuction.setItem(selected);
        newAuction.setCurrentPrice(selected.getStartingPrice());
        newAuction.setStatus(com.auction.shared.model.enums.AuctionStatus.OPEN);
        // Lấy thời gian ưu tiên từ sản phẩm
        if (selected.getPreferredStartTime() != null) {
            newAuction.setStartTime(selected.getPreferredStartTime());
        } else {
            newAuction.setStartTime(LocalDateTime.now());
        }

        if (selected.getPreferredEndTime() != null) {
            newAuction.setEndTime(selected.getPreferredEndTime());
        } else {
            newAuction.setEndTime(LocalDateTime.now().plusDays(3));
        }

        ServerConnection.getInstance().sendRequestAsync(
            new Request(RequestType.CREATE_AUCTION, newAuction),
            response -> {
                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        messageLabel.setStyle("-fx-text-fill: #00ff00;");
                        messageLabel.setText("Tạo phiên đấu giá thành công!");
                        // Load lại danh sách sản phẩm (có thể update trạng thái)
                        loadProducts();
                    } else {
                        messageLabel.setStyle("-fx-text-fill: #ff0000;");
                        messageLabel.setText("Tạo thất bại: " + response.getMessage());
                    }
                });
            }
        );
    }

    @FXML
    private void handleBack() {
        SceneManager.switchTo(
                "/com/auction/fxml/auction/auction-list.fxml"
        );
    }
}
