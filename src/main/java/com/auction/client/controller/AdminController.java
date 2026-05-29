package com.auction.client.controller;


import com.auction.client.util.SceneManager;
import com.auction.shared.model.entity.Auction;
import com.auction.shared.model.entity.Item;
import com.auction.shared.model.entity.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;


import java.util.List;

public class AdminController {

    @FXML
    private TableView<Item> itemTable;
    @FXML
    private TableColumn<Item, String> colItemName;
    @FXML
    private TableColumn<Item, String> colItemCategory;
    @FXML
    private TableColumn<Item, String> colItemPrice;
    @FXML
    private TableColumn<Item, String> colItemStatus;
    @FXML
    private Label itemMessageLabel;

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, String> colUsername;
    @FXML
    private TableColumn<User, String> colEmail;
    @FXML
    private TableColumn<User, String> colRole;
    @FXML
    private Label userMessageLabel;

    @FXML
    private TableView<Auction> auctionTable;
    @FXML
    private TableColumn<Auction, String> colAuctionId;
    @FXML
    private TableColumn<Auction, String> colAuctionItem;
    @FXML
    private TableColumn<Auction, String> colAuctionPrice;
    @FXML
    private TableColumn<Auction, String> colAuctionStatus;
    @FXML
    private Label auctionMessageLabel;

    private List<Item> pendingItems;
    private List<User> allUsers;
    private List<Auction> allAuctionsList; // Đổi tên để phản ánh việc lấy tất cả

    private boolean isProcessingDeleteAuction = false;

    @FXML
    public void initialize() {
        if (itemTable != null) {
            colItemName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
            colItemCategory.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getClass().getSimpleName()));
            colItemPrice.setCellValueFactory(data -> 
                new SimpleStringProperty(String.format("%,.0f VND", data.getValue().getStartingPrice())));
            colItemStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
            loadItemsData();
        }

        if (userTable != null) {
            colUsername.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
            colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
            colRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));
            loadUsersData();
        }
        
        if (auctionTable != null) {
            colAuctionId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
            colAuctionItem.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getItem() != null ? data.getValue().getItem().getName() : ""));
            colAuctionPrice.setCellValueFactory(data -> 
                new SimpleStringProperty(String.format("%,.0f VND", data.getValue().getCurrentPrice())));
            colAuctionStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));
            loadAuctionsData();
        }
    }

    private void loadUsersData() {
        com.auction.shared.protocol.Request req = new com.auction.shared.protocol.Request(
            com.auction.shared.protocol.RequestType.GET_ALL_USERS, null);
        com.auction.client.network.ServerConnection.getInstance().sendRequestAsync(req, res -> {
            Platform.runLater(() -> {
                if (res != null && res.isSuccess() && res.getData() instanceof java.util.List) {
                    allUsers = (java.util.List<User>) res.getData();
                    userTable.setItems(javafx.collections.FXCollections.observableArrayList(allUsers));
                }
            });
        });
    }

    private void loadItemsData() {
        com.auction.shared.protocol.Request req = new com.auction.shared.protocol.Request(
            com.auction.shared.protocol.RequestType.GET_ALL_ITEMS, null);
        com.auction.client.network.ServerConnection.getInstance().sendRequestAsync(req, res -> {
            Platform.runLater(() -> {
                if (res != null && res.isSuccess() && res.getData() instanceof java.util.List) {
                    pendingItems = new java.util.ArrayList<>();
                    for (Item item : (java.util.List<Item>) res.getData()) {
                        if ("PENDING".equals(item.getStatus())) {
                            pendingItems.add(item);
                        }
                    }
                    itemTable.setItems(javafx.collections.FXCollections.observableArrayList(pendingItems));
                }
            });
        });
    }

    @SuppressWarnings("unchecked")
    private void loadAuctionsData() {
        com.auction.shared.protocol.Request req = new com.auction.shared.protocol.Request(
            com.auction.shared.protocol.RequestType.GET_ALL_AUCTIONS, null);
        com.auction.client.network.ServerConnection.getInstance().sendRequestAsync(req, res -> {
            Platform.runLater(() -> {
                if (res != null && res.isSuccess() && res.getData() instanceof java.util.List) {
                    allAuctionsList = new java.util.ArrayList<>();
                    for (Auction auction : (java.util.List<Auction>) res.getData()) {
                        // Bỏ filter OPEN để lấy cả RUNNING và FINISHED
                        allAuctionsList.add(auction);
                    }
                    auctionTable.setItems(javafx.collections.FXCollections.observableArrayList(allAuctionsList));
                }
            });
        });
    }

    @FXML
    private void handleDeleteAuction() {
        if (isProcessingDeleteAuction) return;

        Auction selected = auctionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            auctionMessageLabel.setStyle("-fx-text-fill: #ff6b6b;");
            auctionMessageLabel.setText("Vui lòng chọn phiên đấu giá để xóa!");
            return;
        }

        if (selected.getStatus() != com.auction.shared.model.enums.AuctionStatus.FINISHED) {
            auctionMessageLabel.setStyle("-fx-text-fill: #ff6b6b;");
            auctionMessageLabel.setText("Chỉ có thể xóa phiên đấu giá đã kết thúc (FINISHED)!");
            return;
        }

        isProcessingDeleteAuction = true;
        auctionMessageLabel.setStyle("-fx-text-fill: #ffff00;");
        auctionMessageLabel.setText("Đang xử lý...");

        com.auction.shared.protocol.Request req = new com.auction.shared.protocol.Request(
            com.auction.shared.protocol.RequestType.DELETE_AUCTION, selected.getId());
        
        com.auction.client.network.ServerConnection.getInstance().sendRequestAsync(req, res -> {
            Platform.runLater(() -> {
                isProcessingDeleteAuction = false;
                if (res != null && res.isSuccess()) {
                    auctionMessageLabel.setStyle("-fx-text-fill: #00ff00;");
                    auctionMessageLabel.setText("Đã xóa phiên đấu giá!");
                    loadAuctionsData();
                } else {
                    auctionMessageLabel.setStyle("-fx-text-fill: #ff6b6b;");
                    auctionMessageLabel.setText("Lỗi: " + (res != null ? res.getMessage() : "Mất kết nối"));
                }
            });
        });
    }

    @FXML
    private void handleApproveItem() {
        Item selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            itemMessageLabel.setStyle("-fx-text-fill: #ff6b6b;");
            itemMessageLabel.setText("Vui lòng chọn sản phẩm để duyệt!");
            return;
        }
        selected.setStatus("APPROVED");
        com.auction.shared.protocol.Request req = new com.auction.shared.protocol.Request(
            com.auction.shared.protocol.RequestType.UPDATE_ITEM, selected);
        com.auction.client.network.ServerConnection.getInstance().sendRequestAsync(req, res -> {
            Platform.runLater(() -> {
                if (res != null && res.isSuccess()) {
                    pendingItems.remove(selected);
                    itemTable.getItems().remove(selected);
                    itemMessageLabel.setStyle("-fx-text-fill: #00ff00;");
                    itemMessageLabel.setText("Đã duyệt thành công: " + selected.getName());
                } else {
                    selected.setStatus("PENDING");
                    itemMessageLabel.setStyle("-fx-text-fill: #ff6b6b;");
                    itemMessageLabel.setText("Lỗi duyệt: " + (res != null ? res.getMessage() : "Không phản hồi"));
                }
            });
        });
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
        com.auction.shared.protocol.Request req = new com.auction.shared.protocol.Request(
            com.auction.shared.protocol.RequestType.UPDATE_ITEM, selected);
        com.auction.client.network.ServerConnection.getInstance().sendRequestAsync(req, res -> {
            Platform.runLater(() -> {
                if (res != null && res.isSuccess()) {
                    pendingItems.remove(selected);
                    itemTable.getItems().remove(selected);
                    itemMessageLabel.setStyle("-fx-text-fill: #e65100;");
                    itemMessageLabel.setText("Đã từ chối: " + selected.getName());
                } else {
                    selected.setStatus("PENDING");
                    itemMessageLabel.setStyle("-fx-text-fill: #ff6b6b;");
                    itemMessageLabel.setText("Lỗi từ chối: " + (res != null ? res.getMessage() : "Không phản hồi"));
                }
            });
        });
    }

    @FXML
    private void handleBanUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            userMessageLabel.setStyle("-fx-text-fill: #ff6b6b;");
            userMessageLabel.setText("Vui lòng chọn người dùng để khóa!");
            return;
        }
        if ("ADMIN".equals(selected.getRole())) {
            userMessageLabel.setStyle("-fx-text-fill: #ff6b6b;");
            userMessageLabel.setText("Không thể khóa tài khoản Quản trị viên!");
            return;
        }
        
        com.auction.shared.protocol.Request req = new com.auction.shared.protocol.Request(
            com.auction.shared.protocol.RequestType.BAN_USER, selected.getId());
        com.auction.client.network.ServerConnection.getInstance().sendRequestAsync(req, res -> {
            Platform.runLater(() -> {
                if (res != null && res.isSuccess()) {
                    userMessageLabel.setStyle("-fx-text-fill: #00ff00;");
                    userMessageLabel.setText("Đã khóa tài khoản thành công: " + selected.getUsername());
                    selected.setStatus(com.auction.shared.model.enums.UserStatus.BANNED);
                    userTable.refresh();
                } else {
                    userMessageLabel.setStyle("-fx-text-fill: #ff6b6b;");
                    userMessageLabel.setText("Lỗi khóa: " + (res != null ? res.getMessage() : "Không phản hồi"));
                }
            });
        });
    }

    @FXML
    private void handleStartAuction() {
        Auction selected = auctionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            auctionMessageLabel.setStyle("-fx-text-fill: #ff6b6b;");
            auctionMessageLabel.setText("Vui lòng chọn phiên đấu giá!");
            return;
        }
        com.auction.shared.protocol.Request req = new com.auction.shared.protocol.Request(
            com.auction.shared.protocol.RequestType.START_AUCTION, selected.getId());
        com.auction.client.network.ServerConnection.getInstance().sendRequestAsync(req, res -> {
            Platform.runLater(() -> {
                if (res != null && res.isSuccess()) {
                    auctionMessageLabel.setStyle("-fx-text-fill: #00ff00;");
                    auctionMessageLabel.setText("Đã bắt đầu phiên thành công!");
                    loadAuctionsData();
                } else {
                    auctionMessageLabel.setStyle("-fx-text-fill: #ff6b6b;");
                    auctionMessageLabel.setText("Lỗi: " + (res != null ? res.getMessage() : "Không phản hồi"));
                }
            });
        });
    }

    @FXML
    private void goToManageItems() {
        SceneManager.switchTo("/com/auction/fxml/admin/admin-items.fxml");
    }

    @FXML
    private void goToManageUsers() {
        SceneManager.switchTo("/com/auction/fxml/admin/admin-users.fxml");
    }

    @FXML
    private void goToManageAuctions() {
        SceneManager.switchTo("/com/auction/fxml/admin/admin-auctions.fxml");
    }

    @FXML
    private void goToProfile() {
        SceneManager.switchTo("/com/auction/fxml/auth/profile.fxml");
    }

    @FXML
    private void handleLogout() {
        SceneManager.switchTo("/com/auction/fxml/auth/login.fxml");
    }
}
