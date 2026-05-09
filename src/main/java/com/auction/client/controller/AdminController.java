package com.auction.client.controller;

import com.auction.client.util.MockDataService;
import com.auction.client.util.SceneManager;
import com.auction.shared.model.entity.Item;
import com.auction.shared.model.entity.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;

public class AdminController {

    // --- BIẾN CHO TAB SẢN PHẨM ---
    @FXML private TableView<Item> itemTable;
    @FXML private TableColumn<Item, String> colItemName;
    @FXML private TableColumn<Item, String> colItemCategory;
    @FXML private TableColumn<Item, String> colItemPrice;
    @FXML private TableColumn<Item, String> colItemStatus;
    @FXML private Label itemMessageLabel;

    // --- BIẾN CHO TAB NGƯỜI DÙNG ---
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;
    @FXML private Label userMessageLabel;

    private List<Item> pendingItems;
    private List<User> allUsers;

    @FXML
    public void initialize() {
        // Cấu hình Cột Sản phẩm
        colItemName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colItemCategory.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getClass().getSimpleName()));
        colItemPrice.setCellValueFactory(data -> new SimpleStringProperty(String.format("%,.0f VND", data.getValue().getStartingPrice())));
        colItemStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        // Cấu hình Cột Người dùng
        colUsername.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));

        loadMockData();
    }

    private void loadMockData() {
        // Lấy 1 Sản phẩm từ MockData và ép nó về trạng thái "Đang chờ duyệt (PENDING)"
        pendingItems = new ArrayList<>();
        if(!MockDataService.getFakeAuctions().isEmpty()){
            Item mockItem = MockDataService.getFakeAuctions().get(0).getItem();
            mockItem.setStatus("PENDING");
            pendingItems.add(mockItem);
        }
        itemTable.setItems(FXCollections.observableArrayList(pendingItems));

        // Lấy thông tin User từ MockData
        allUsers = new ArrayList<>();
        allUsers.add(MockDataService.getFakeUser());
        userTable.setItems(FXCollections.observableArrayList(allUsers));
    }

    // ================= XỬ LÝ SẢN PHẨM =================
    @FXML
    private void handleApproveItem() {
        Item selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            itemMessageLabel.setStyle("-fx-text-fill: #ff6b6b;");
            itemMessageLabel.setText("Vui lòng chọn sản phẩm để duyệt!");
            return;
        }
        selected.setStatus("APPROVED");
        itemTable.refresh(); // Cập nhật lại màu sắc chữ trên bảng
        itemMessageLabel.setStyle("-fx-text-fill: #00ff00;");
        itemMessageLabel.setText("Đã duyệt thành công: " + selected.getName());
    }

    @FXML
    private void handleRejectItem() {
        Item selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            itemMessageLabel.setStyle("-fx-text-fill: #ff6b6b;");
            itemMessageLabel.setText("Vui lòng chọn sản phẩm để từ chối!");
            return;
        }
        selected.setStatus("REJECTED");
        itemTable.refresh();
        itemMessageLabel.setStyle("-fx-text-fill: #e65100;"); // Màu cam cảnh báo
        itemMessageLabel.setText("Đã từ chối: " + selected.getName());
    }

    // ================= XỬ LÝ NGƯỜI DÙNG =================
    @FXML
    private void handleBanUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            userMessageLabel.setStyle("-fx-text-fill: #ff6b6b;");
            userMessageLabel.setText("Vui lòng chọn người dùng để khóa!");
            return;
        }
        userMessageLabel.setStyle("-fx-text-fill: #00ff00;");
        userMessageLabel.setText("Đã khóa tài khoản thành công: " + selected.getUsername());
    }

    @FXML
    private void handleLogout() {
        SceneManager.switchTo("/com/auction/fxml/auth/login.fxml");
    }
}