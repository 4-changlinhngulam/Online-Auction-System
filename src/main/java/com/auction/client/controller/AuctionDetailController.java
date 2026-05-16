package com.auction.client.controller;

import com.auction.client.util.MockDataService;
import com.auction.client.util.SceneManager;
import com.auction.shared.model.entity.Auction;
import com.auction.shared.model.entity.BidTransaction;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.List;

public class AuctionDetailController {

    @FXML private Label itemNameLabel;
    @FXML private Label itemDescLabel;
    @FXML private Label currentPriceLabel;
    @FXML private Label timeRemainingLabel;
    @FXML private Label statusLabel;
    @FXML private TextField bidAmountField;
    @FXML private Button placeBidButton;
    @FXML private Label bidErrorLabel;
    @FXML private ListView<String> bidHistoryList;

    private Auction currentAuction;

    @FXML
    public void initialize() {
        // Load mock data
        currentAuction = MockDataService.getFakeAuctions().get(0);
        displayAuction(currentAuction);
        loadBidHistory();
    }

    private void displayAuction(Auction auction) {
        itemNameLabel.setText("Sản phẩm: " + auction.getItem().getName());
        currentPriceLabel.setText(
                String.format("%,.0f VND", auction.getCurrentPrice())
        );
        statusLabel.setText(auction.getStatus().toString());
        timeRemainingLabel.setText("Đang tính...");
    }

    private void loadBidHistory() {
        // --- MOCK MODE ---
        List<BidTransaction> history = MockDataService
                .getFakeBidHistory(currentAuction.getId());

        bidHistoryList.getItems().clear();
        for (BidTransaction bid : history) {
            bidHistoryList.getItems().add(
                    String.format("%s đặt: %,.0f VND",
                            bid.getBidderId(), bid.getAmount())
            );
        }
    }

    @FXML
    private void handlePlaceBid() {
        String amountStr = bidAmountField.getText().trim();

        if (amountStr.isEmpty()) {
            bidErrorLabel.setText("Vui lòng nhập số tiền!");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);

            if (amount <= currentAuction.getCurrentPrice()) {
                bidErrorLabel.setText(
                        "Giá phải cao hơn " +
                                String.format("%,.0f VND",
                                        currentAuction.getCurrentPrice())
                );
                return;
            }

            // --- MOCK MODE ---
            currentAuction.setCurrentPrice(amount);
            currentPriceLabel.setText(
                    String.format("%,.0f VND", amount)
            );
            bidHistoryList.getItems().add(0,
                    "Bạn vừa đặt: " +
                            String.format("%,.0f VND", amount)
            );
            bidErrorLabel.setStyle("-fx-text-fill: #00ff00;");
            bidErrorLabel.setText("Đặt giá thành công!");
            bidAmountField.clear();

            // --- REAL MODE ---
            // Request req = new Request(RequestType.PLACE_BID,
            //     new Object[]{currentAuction.getId(),
            //                  SessionManager.getInstance()
            //                                .getCurrentUser().getId(),
            //                  amount});
            // Response res = ServerConnection.getInstance()
            //                                .sendRequest(req);

        } catch (NumberFormatException e) {
            bidErrorLabel.setText("Số tiền không hợp lệ!");
        }
    }

    /** Observer — được gọi khi Server push cập nhật realtime */
    public void onBidUpdate(Auction updatedAuction) {
        Platform.runLater(() -> {
            currentAuction = updatedAuction;
            currentPriceLabel.setText(
                    String.format("%,.0f VND",
                            updatedAuction.getCurrentPrice())
            );
            loadBidHistory();
        });
    }

    @FXML
    private void handleBack() {
        SceneManager.switchTo(
                "/com/auction/fxml/auction/auction-list.fxml"
        );
    }
}
