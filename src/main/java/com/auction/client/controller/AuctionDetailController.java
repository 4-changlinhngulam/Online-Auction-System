package com.auction.client.controller;

import com.auction.client.network.ServerConnection;

import com.auction.client.util.SceneManager;
import com.auction.client.util.SessionManager;
import com.auction.shared.model.entity.Auction;
import com.auction.shared.model.entity.BidTransaction;
import com.auction.shared.protocol.Request;
import com.auction.shared.protocol.RequestType;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import java.util.Optional;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.time.LocalDateTime;



public class AuctionDetailController {

    @FXML
    private Label itemNameLabel;
    @FXML
    private Label itemDescLabel;
    @FXML
    private Label currentPriceLabel;
    @FXML
    private Label timeRemainingLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private TextField bidAmountField;
    @FXML
    private Button placeBidButton;
    @FXML
    private Label bidErrorLabel;
    @FXML
    private ListView<String> bidHistoryList;

    private Auction currentAuction;
    private Timeline countdownTimeline;

    @FXML
    public void initialize() {
        currentAuction = com.auction.client.util.SessionManager.getInstance().getCurrentAuction();
        if (currentAuction != null) {
            displayAuction(currentAuction);
            loadBidHistory();
        }
    }

    private void displayAuction(Auction auction) {
        itemNameLabel.setText("Sản phẩm: " + auction.getItem().getName());
        currentPriceLabel.setText(
                String.format("%,.0f VND", auction.getCurrentPrice()));
        statusLabel.setText(auction.getStatus().toString());
        if ("FINISHED".equals(auction.getStatus().toString())) {
            statusLabel.setStyle("-fx-text-fill: #9e9e9e; -fx-font-weight: bold;"); // Màu xám cho FINISHED
        } else {
            statusLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;"); // Màu xanh cho RUNNING
        }

        startCountdown(auction);
    }

    private void startCountdown(Auction auction) {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endTime = auction.getEndTime();

            if (endTime == null) {
                timeRemainingLabel.setText("Không xác định");
                return;
            }

            java.time.Duration duration = java.time.Duration.between(now, endTime);
            long seconds = duration.getSeconds();

            if (seconds > 0) {
                long h = seconds / 3600;
                long m = (seconds % 3600) / 60;
                long s = seconds % 60;
                timeRemainingLabel.setText(String.format("%02d:%02d:%02d", h, m, s));
            } else {
                timeRemainingLabel.setText("00:00:00");
                statusLabel.setText("FINISHED");
                statusLabel.setStyle("-fx-text-fill: #9e9e9e; -fx-font-weight: bold;");
                countdownTimeline.stop();
            }
        }));

        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void loadBidHistory() {
        // --- REAL MODE ---
        try {
            Request req = new Request(RequestType.GET_BID_HISTORY, currentAuction.getId());
            ServerConnection.getInstance().sendRequestAsync(req, res -> {
                if (res != null && res.isSuccess() && res.getData() instanceof java.util.List) {
                    java.util.List<BidTransaction> history = (java.util.List<BidTransaction>) res.getData();
                    bidHistoryList.getItems().clear();
                    for (BidTransaction bid : history) {
                        bidHistoryList.getItems().add(
                                String.format("%s đặt: %,.0f VND", bid.getBidderId(), bid.getAmount())
                        );
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
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
                                        currentAuction.getCurrentPrice()));
                return;
            }

            bidAmountField.clear();
            Request req = new Request(RequestType.PLACE_BID,
                    new Object[] {
                            currentAuction.getId(),
                            SessionManager.getInstance().getCurrentUser().getId(),
                            amount
                    });
            ServerConnection.getInstance().sendRequestAsync(req, res -> {
                if (res != null && res.isSuccess()) {
                    currentAuction.setCurrentPrice(amount);
                    currentPriceLabel.setText(String.format("%,.0f VND", amount));
                    bidHistoryList.getItems().add(0, "Bạn vừa đặt: " + String.format("%,.0f VND", amount));
                    bidErrorLabel.setStyle("-fx-text-fill: #00ff00;");
                    bidErrorLabel.setText("Đặt giá thành công!");
                } else {
                    bidErrorLabel.setStyle("-fx-text-fill: #ff0000;");
                    bidErrorLabel.setText(res != null ? res.getMessage() : "Lỗi kết nối");
                }
            });

        } catch (NumberFormatException e) {
            bidErrorLabel.setText("Số tiền không hợp lệ!");
        } catch (Exception e) {
            bidErrorLabel.setText("Đã có lỗi xảy ra!");
        }
    }

    /** Observer — được gọi khi Server push cập nhật realtime */
    public void onBidUpdate(com.auction.shared.model.entity.Item item, double newPrice, String lastBidderId,
            java.time.LocalDateTime newEndTime) {
        Platform.runLater(() -> {
            if (currentAuction != null && currentAuction.getItem().getId().equals(item.getId())) {
                currentAuction.setCurrentPrice(newPrice);

                // Anti-sniping: Kiểm tra nếu thời gian được kéo dài
                if (newEndTime != null && !newEndTime.equals(currentAuction.getEndTime())) {
                    currentAuction.setEndTime(newEndTime);
                    bidErrorLabel.setStyle("-fx-text-fill: #ff9900;");
                    bidErrorLabel.setText("Phút chót! Thời gian đã được gia hạn.");
                }

                currentPriceLabel.setText(
                        String.format("%,.0f VND", newPrice));
                loadBidHistory();
            }
        });
    }

    @FXML
    private void handleAutoBidSetup() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cài đặt Auto-Bid");
        dialog.setHeaderText("Hệ thống sẽ tự động đặt giá thay bạn (bước giá 50.000 VND).");
        dialog.setContentText("Nhập giá trần tối đa (VND):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                double maxAmount = Double.parseDouble(amountStr);
                if (maxAmount <= currentAuction.getCurrentPrice()) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Giá trần phải cao hơn giá hiện tại!");
                    return;
                }

                // --- Gửi Request thật lên Server (nếu mở mạng) ---
                Request req = new Request(RequestType.SETUP_AUTO_BID,
                        new Object[] {
                                currentAuction.getId(),
                                SessionManager.getInstance().getCurrentUser().getId(),
                                maxAmount
                        });
                ServerConnection.getInstance().sendRequestAsync(req, res -> {
                    if (res != null && res.isSuccess()) {
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", res.getMessage());
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", res != null ? res.getMessage() : "Mất kết nối");
                    }
                });
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập số hợp lệ!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể gửi yêu cầu: " + e.getMessage());
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        SceneManager.switchTo(
                "/com/auction/fxml/auction/auction-list.fxml");
    }
}
