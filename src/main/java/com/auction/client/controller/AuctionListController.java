package com.auction.client.controller;
import com.auction.shared.model.entity.Auction;
import javafx.fxml.FXML;
import javafx.scene.control.*;
/** Controller cho auction-list.fxml */
public class AuctionListController {
    @FXML private TableView<Auction> auctionTable;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML
    public void initialize() {
        // TODO: Gửi Request(GET_ALL_AUCTIONS) → populate TableView
        // TODO: Double-click → mở AuctionDetailController với auction đã chọn
    }
    @FXML private void handleSearch() { /* TODO: filter */ }
    @FXML private void handleRefresh() { /* TODO: reload */ }
}
