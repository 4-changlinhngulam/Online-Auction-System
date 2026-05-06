package com.auction.client.controller;
import com.auction.shared.model.entity.Auction;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.chart.LineChart;
/** Controller cho auction-detail.fxml – đấu giá realtime + Observer. */
public class AuctionDetailController {
    @FXML private Label currentPriceLabel, timeRemainingLabel;
    @FXML private TextField bidAmountField;
    @FXML private Button placeBidButton;
    @FXML private ListView<String> bidHistoryList;
    @FXML private LineChart<String, Number> priceChart; // Bonus visualization

    private Auction currentAuction;

    public void setAuction(Auction auction) {
        this.currentAuction = auction;
        // TODO: Hiển thị thông tin, subscribe realtime (SUBSCRIBE_AUCTION)
    }
    @FXML
    private void handlePlaceBid() {
        // TODO: Gửi Request(PLACE_BID) → xử lý InvalidBidException, AuctionClosedException
    }
    /** Được gọi khi Server push cập nhật (Observer pattern). */
    public void onBidUpdate(Auction updatedAuction) {
        // TODO: Platform.runLater → cập nhật currentPriceLabel, bidHistoryList, priceChart
    }
}
