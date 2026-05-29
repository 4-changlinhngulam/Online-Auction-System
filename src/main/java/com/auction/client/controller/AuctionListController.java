package com.auction.client.controller;

import com.auction.client.network.ServerConnection;

import com.auction.client.util.SceneManager;
import com.auction.client.util.SessionManager;
import com.auction.shared.model.entity.Auction;
import com.auction.shared.protocol.Request;
import com.auction.shared.protocol.RequestType;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.stream.Collectors;

public class AuctionListController {

        @FXML
        private TextField searchField;
        @FXML
        private ComboBox<String> statusFilter;
        @FXML
        private TableView<Auction> auctionTable;
        @FXML
        private TableColumn<Auction, String> colName;
        @FXML
        private TableColumn<Auction, String> colCurrentPrice;
        @FXML
        private TableColumn<Auction, String> colStatus;
        @FXML
        private TableColumn<Auction, String> colEndTime;
        @FXML
        private Label messageLabel;
        @FXML
        private Button btnMyProducts;
        @FXML
        private Button btnCreateAuction;

        private List<Auction> allAuctions;

        @FXML
        public void initialize() {
                // Setup ComboBox trạng thái
                statusFilter.setItems(FXCollections.observableArrayList(
                                "Tất cả", "OPEN", "RUNNING", "FINISHED"));
                statusFilter.setValue("Tất cả");

                // Thêm listener tự động tìm kiếm
                searchField.textProperty().addListener((obs, oldV, newV) -> handleSearch());
                statusFilter.valueProperty().addListener((obs, oldV, newV) -> handleSearch());

                // Setup cột TableView
                colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                                data.getValue().getItem().getName()));
                colCurrentPrice.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                                String.format("%,.0f VND", data.getValue().getCurrentPrice())));
                colStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                                data.getValue().getStatus().toString()));
                colEndTime.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                                data.getValue().getEndTime() != null ? 
                                data.getValue().getEndTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : 
                                "Không xác định"));
                
                // Ẩn chức năng Seller nếu là BIDDER
                String role = SessionManager.getInstance().getCurrentUser().getRole();
                if ("BIDDER".equals(role)) {
                        btnMyProducts.setVisible(false);
                        btnMyProducts.setManaged(false);
                        btnCreateAuction.setVisible(false);
                        btnCreateAuction.setManaged(false);
                }

                // Double click → vào chi tiết
                auctionTable.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2) {
                                Auction selected = auctionTable.getSelectionModel()
                                                .getSelectedItem();
                                if (selected != null) {
                                        goToAuctionDetail(selected);
                                }
                        }
                });

                // Load dữ liệu
                loadAuctions();
        }

        private void loadAuctions() {
                ServerConnection.getInstance().sendRequestAsync(
                                new Request(RequestType.GET_ALL_AUCTIONS, null),
                                res -> {
                                        if (res.isSuccess()) {
                                                allAuctions = (List<Auction>) res.getData();
                                                auctionTable.setItems(FXCollections.observableArrayList(allAuctions));
                                        }
                                });

        }

        @FXML
        private void handleSearch() {
                if (allAuctions == null) return;
                
                String keyword = searchField.getText().trim().toLowerCase();
                String status = statusFilter.getValue();
                if (status == null) status = "Tất cả";

                final String finalStatus = status;

                List<Auction> filtered = allAuctions.stream()
                                .filter(a -> {
                                        boolean matchKeyword = keyword.isEmpty() ||
                                                        a.getItem().getName().toLowerCase().contains(keyword);
                                        boolean matchStatus = finalStatus.equals("Tất cả") ||
                                                        a.getStatus().toString().equals(finalStatus);
                                        return matchKeyword && matchStatus;
                                })
                                .collect(Collectors.toList());

                auctionTable.setItems(
                                FXCollections.observableArrayList(filtered));
        }

        @FXML
        private void handleRefresh() {
                searchField.clear();
                statusFilter.setValue("Tất cả");
                loadAuctions();
        }

        private void goToAuctionDetail(Auction auction) {
                com.auction.client.util.SessionManager.getInstance().setCurrentAuction(auction);
                SceneManager.switchTo(
                                "/com/auction/fxml/auction/auction-detail.fxml");
        }

        @FXML
        private void goToAuctionList() {
                SceneManager.switchTo(
                                "/com/auction/fxml/auction/auction-list.fxml");
        }

        @FXML
        private void goToMyProducts() {
                SceneManager.switchTo(
                                "/com/auction/fxml/product/product-manage.fxml");
        }

        @FXML
        private void goToCreateAuction() {
                SceneManager.switchTo(
                                "/com/auction/fxml/product/product-form.fxml");
        }

        @FXML
        private void goToProfile() {
                SceneManager.switchTo("/com/auction/fxml/auth/profile.fxml");
        }

        @FXML
        private void handleLogout() {
                com.auction.client.util.SessionManager.getInstance()
                                .setCurrentUser(null);
                SceneManager.switchTo("/com/auction/fxml/auth/login.fxml");
        }
}
