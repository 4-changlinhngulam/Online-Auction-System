package com.auction.client.controller;

import com.auction.client.util.MockDataService;
import com.auction.client.util.SceneManager;
import com.auction.shared.model.entity.Auction;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.stream.Collectors;

public class AuctionListController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<Auction> auctionTable;
    @FXML private TableColumn<Auction, String> colName;
    @FXML private TableColumn<Auction, String> colCurrentPrice;
    @FXML private TableColumn<Auction, String> colStatus;
    @FXML private TableColumn<Auction, String> colEndTime;
    @FXML private Label messageLabel;

    private List<Auction> allAuctions;

    @FXML
    public void initialize() {
        // Setup ComboBox trạng thái
        statusFilter.setItems(FXCollections.observableArrayList(
                "Tất cả", "OPEN", "RUNNING", "FINISHED"
        ));
        statusFilter.setValue("Tất cả");

        // Setup cột TableView
        colName.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getItem().getName()
                )
        );
        colCurrentPrice.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%,.0f VND", data.getValue().getCurrentPrice())
                )
        );
        colStatus.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getStatus().toString()
                )
        );
        colEndTime.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getEndTime()
                                .format(java.time.format.DateTimeFormatter
                                        .ofPattern("dd/MM/yyyy HH:mm"))
                )
        );

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
        // --- MOCK MODE ---
        allAuctions = MockDataService.getFakeAuctions();
        auctionTable.setItems(
                FXCollections.observableArrayList(allAuctions)
        );

        // --- REAL MODE ---
        // Request req = new Request(RequestType.GET_ALL_AUCTIONS, null);
        // Response res = ServerConnection.getInstance().sendRequest(req);
        // allAuctions = (List<Auction>) res.getData();
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim().toLowerCase();
        String status = statusFilter.getValue();

        List<Auction> filtered = allAuctions.stream()
                .filter(a -> {
                    boolean matchKeyword = keyword.isEmpty() ||
                            a.getItem().getName().toLowerCase().contains(keyword);
                    boolean matchStatus = status.equals("Tất cả") ||
                            a.getStatus().toString().equals(status);
                    return matchKeyword && matchStatus;
                })
                .collect(Collectors.toList());

        auctionTable.setItems(
                FXCollections.observableArrayList(filtered)
        );
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        statusFilter.setValue("Tất cả");
        loadAuctions();
    }

    private void goToAuctionDetail(Auction auction) {
        SceneManager.switchTo(
                "/com/auction/fxml/auction/auction-detail.fxml"
        );
        // TODO: truyền auction sang AuctionDetailController
    }

    @FXML private void goToAuctionList() {
        SceneManager.switchTo(
                "/com/auction/fxml/auction/auction-list.fxml"
        );
    }

    @FXML private void goToMyProducts() {
        SceneManager.switchTo(
                "/com/auction/fxml/product/product-manage.fxml"
        );
    }

    @FXML private void goToCreateAuction() {
        SceneManager.switchTo(
                "/com/auction/fxml/product/product-form.fxml"
        );
    }

    @FXML private void handleLogout() {
        SceneManager.switchTo(
                "/com/auction/fxml/auth/login.fxml"
        );
    }
}
