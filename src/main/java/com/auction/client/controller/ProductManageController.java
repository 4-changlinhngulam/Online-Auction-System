package com.auction.client.controller;
import javafx.fxml.FXML;
import javafx.scene.control.*;
/** Controller cho product-manage.fxml – CRUD sản phẩm của Seller. */
public class ProductManageController {
    @FXML private TableView<?> itemTable;
    @FXML
    public void initialize() { /* TODO: Load items của Seller hiện tại */ }
    @FXML private void handleCreateItem() { /* TODO: Mở dialog → Request(CREATE_ITEM) */ }
    @FXML private void handleCreateAuction() { /* TODO: Dialog thiết lập phiên → Request(CREATE_AUCTION) */ }
    @FXML private void handleDeleteItem() { /* TODO: Xác nhận → Request(DELETE_ITEM) */ }
}
