package com.auction.client.controller;
import javafx.fxml.FXML;
import javafx.scene.control.*;
/** Controller cho admin-dashboard.fxml */
public class AdminController {
    @FXML private TableView<?> userTable, auctionTable;
    @FXML
    public void initialize() { /* TODO: GET_ALL_USERS, GET_ALL_AUCTIONS */ }
    @FXML private void handleBanUser() { /* TODO: Request(BAN_USER) */ }
}
