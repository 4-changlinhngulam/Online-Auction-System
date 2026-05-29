package com.auction.client.controller;
import com.auction.client.network.ServerConnection;
import com.auction.client.util.SceneManager;
import com.auction.client.util.SessionManager;
import com.auction.shared.model.entity.Auction;
import com.auction.shared.model.entity.BidTransaction;
import com.auction.shared.model.entity.Item;
import com.auction.shared.protocol.Request;
import com.auction.shared.protocol.RequestType;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.io.ByteArrayInputStream;
public class AuctionDetailController {
    @FXML private ImageView itemImageView;
    @FXML private Label imagePlaceholderLabel;
    @FXML private Label itemNameLabel;
    @FXML private Label itemCategoryLabel;
    @FXML private Label itemDescLabel;
    @FXML private Label startingPriceLabel;
    @FXML private Label currentPriceLabel;
    @FXML private Label leadingBidderLabel;
    @FXML private Label timeRemainingLabel;
    @FXML private Label statusLabel;
    @FXML private LineChart<String, Number> bidPriceChart;
    @FXML private CategoryAxis chartXAxis;
    @FXML private NumberAxis chartYAxis;
    @FXML private ListView<String> bidHistoryList;
    private XYChart.Series<String, Number> priceSeries;
    @FXML private TextField bidAmountField;
    @FXML private Button placeBidButton;
    @FXML private Button autoBidButton;
    @FXML private Label bidErrorLabel;
    @FXML private VBox autoBidInfoPane;
    @FXML private Label autoBidMaxLabel;
    @FXML private Label autoBidIncrementLabel;
    @FXML private Label sellerNameLabel;
    @FXML private Label sellerRatingLabel;
    private Auction currentAuction;
    private Timeline countdownTimeline;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    @FXML
    public void initialize() {
        // Cấu hình UI ban đầu
        setupNumericValidation();
        autoBidInfoPane.setVisible(false);
        autoBidInfoPane.setManaged(false);
        // Khởi tạo biểu đồ
        priceSeries = new XYChart.Series<>();
        priceSeries.setName("Diễn biến giá (VND)");
        bidPriceChart.getData().add(priceSeries);
        // Lấy dữ liệu phiên
        currentAuction = SessionManager.getInstance().getCurrentAuction();
        if (currentAuction != null) {
            displayAuction(currentAuction);
            loadBidHistory();
        }
    }
    private void displayAuction(Auction auction) {
        Item item = auction.getItem();
        // Thông tin cơ bản
        itemNameLabel.setText(item.getName() != null ? item.getName() : "Chưa có tên");
        itemDescLabel.setText(item.getDescription() != null ? item.getDescription() : "Chưa có mô tả");
        // Hiển thị hình ảnh
        if (item.getImageBytes() != null && item.getImageBytes().length > 0) {
            javafx.scene.image.Image img = new javafx.scene.image.Image(new ByteArrayInputStream(item.getImageBytes()));
            itemImageView.setImage(img);
            if (imagePlaceholderLabel != null) {
                imagePlaceholderLabel.setVisible(false);
            }
        }
        // Thông tin nâng cao
        itemCategoryLabel.setText("Danh mục: " + item.getClass().getSimpleName());
        startingPriceLabel.setText(String.format("%,.0f VND", item.getStartingPrice()));
        sellerNameLabel.setText(item.getOwnerName() != null ? item.getOwnerName() : "Ẩn danh");
        sellerRatingLabel.setVisible(false); // Ẩn đánh giá vì chưa hỗ trợ
        currentPriceLabel.setText(String.format("%,.0f VND", auction.getCurrentPrice()));
        // Trạng thái phiên
        statusLabel.setText(auction.getStatus().toString());
        if ("FINISHED".equals(auction.getStatus().toString())) {
            statusLabel.setStyle("-fx-text-fill: #9e9e9e; -fx-font-weight: bold;");
            placeBidButton.setDisable(true);
            autoBidButton.setDisable(true);
        } else {
            statusLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
        }
        // Điểm giá khởi tạo sẽ được nạp trong loadBidHistory() thay vì gán cứng
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
                placeBidButton.setDisable(true);
                autoBidButton.setDisable(true);
                countdownTimeline.stop();
            }
        }));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }
    private void loadBidHistory() {
        try {
            Request req = new Request(RequestType.GET_BID_HISTORY, currentAuction.getId());
            ServerConnection.getInstance().sendRequestAsync(req, res -> {
                if (res != null && res.isSuccess() && res.getData() instanceof java.util.List) {
                    java.util.List<BidTransaction> history = (java.util.List<BidTransaction>) res.getData();
                    Platform.runLater(() -> {
                        bidHistoryList.getItems().clear();
                        priceSeries.getData().clear();
                        
                        // Thêm điểm bắt đầu phiên đấu giá vào biểu đồ
                        if (currentAuction.getStartTime() != null) {
                            updateChartData(currentAuction.getStartTime().format(timeFormatter), currentAuction.getItem().getStartingPrice());
                        }
                        for (BidTransaction bid : history) {
                            // Cập nhật danh sách (hiển thị giao dịch mới nhất ở trên cùng)
                            bidHistoryList.getItems().add(0,
                                    String.format("%s đặt: %,.0f VND", bid.getBidderId(), bid.getAmount())
                            );
                            
                            // Thêm điểm vào biểu đồ (thêm tuần tự theo thời gian)
                            updateChartData(bid.getTimestamp().format(timeFormatter), bid.getAmount());
                        }
                        // Cập nhật người dẫn đầu dựa trên lịch sử (lịch sử DB trả về ASC, phần tử cuối cùng là mới nhất)
                        if (!history.isEmpty()) {
                            leadingBidderLabel.setText(history.get(history.size() - 1).getBidderId());
                        }
                    });
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
                bidErrorLabel.setText("Giá phải cao hơn " + String.format("%,.0f VND", currentAuction.getCurrentPrice()));
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
                Platform.runLater(() -> {
                    if (res != null && res.isSuccess()) {
                        // Lưu ý: Việc cập nhật UI chi tiết thường được ưu tiên xử lý qua onBidUpdate (Observer)
                        // để đồng bộ với tất cả client. Ở đây có thể chỉ cần hiển thị thông báo.
                        bidErrorLabel.setStyle("-fx-text-fill: #00ff00;");
                        bidErrorLabel.setText("Đặt giá thành công!");
                    } else {
                        bidErrorLabel.setStyle("-fx-text-fill: #ff0000;");
                        bidErrorLabel.setText(res != null ? res.getMessage() : "Lỗi kết nối");
                    }
                });
            });
        } catch (NumberFormatException e) {
            bidErrorLabel.setText("Số tiền không hợp lệ!");
        } catch (Exception e) {
            bidErrorLabel.setText("Đã có lỗi xảy ra!");
        }
    }
    /** * Observer — được gọi khi Server push cập nhật realtime cho TẤT CẢ client
     */
    public void onBidUpdate(Item item, double newPrice, String lastBidderId, LocalDateTime newEndTime) {
        Platform.runLater(() -> {
            if (currentAuction != null && currentAuction.getItem().getId().equals(item.getId())) {
                currentAuction.setCurrentPrice(newPrice);
                // Anti-sniping: Gia hạn thời gian
                if (newEndTime != null && !newEndTime.equals(currentAuction.getEndTime())) {
                    currentAuction.setEndTime(newEndTime);
                    bidErrorLabel.setStyle("-fx-text-fill: #ff9900;");
                    bidErrorLabel.setText("Phút chót! Thời gian đã được gia hạn.");
                }
                // Cập nhật nhãn giá và người dẫn đầu
                currentPriceLabel.setText(String.format("%,.0f VND", newPrice));
                leadingBidderLabel.setText(lastBidderId != null ? lastBidderId : "--");
                // Cập nhật biểu đồ
                updateChartData(LocalDateTime.now().format(timeFormatter), newPrice);
                // Tải lại lịch sử
                loadBidHistory();
            }
        });
    }
    @FXML
    private void handleAutoBidSetup() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cài đặt Auto-Bid");
        dialog.setHeaderText("Hệ thống sẽ tự động đặt giá thay bạn.");
        dialog.setContentText("Nhập giá trần tối đa (VND):");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                double maxAmount = Double.parseDouble(amountStr);
                if (maxAmount <= currentAuction.getCurrentPrice()) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Giá trần phải cao hơn giá hiện tại!");
                    return;
                }
                Request req = new Request(RequestType.SETUP_AUTO_BID,
                        new Object[] {
                                currentAuction.getId(),
                                SessionManager.getInstance().getCurrentUser().getId(),
                                maxAmount
                        });
                ServerConnection.getInstance().sendRequestAsync(req, res -> {
                    Platform.runLater(() -> {
                        if (res != null && res.isSuccess()) {
                            enableAutoBidUI(maxAmount, 50000); // Mặc định bước giá 50k
                            showAlert(Alert.AlertType.INFORMATION, "Thành công", res.getMessage());
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Lỗi", res != null ? res.getMessage() : "Mất kết nối");
                        }
                    });
                });
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập số hợp lệ!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể gửi yêu cầu: " + e.getMessage());
            }
        });
    }
    @FXML
    private void handleCancelAutoBid(ActionEvent event) {
        if (currentAuction == null) return;
        
        Request req = new Request(RequestType.CANCEL_AUTO_BID, currentAuction.getId());
        ServerConnection.getInstance().sendRequestAsync(req, res -> {
            Platform.runLater(() -> {
                if (res != null && res.isSuccess()) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã hủy Auto-bid!");
                    disableAutoBidUI();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể hủy Auto-bid: " + (res != null ? res.getMessage() : ""));
                }
            });
        });
    }
    @FXML
    private void handleBack() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        SceneManager.switchTo("/com/auction/fxml/auction/auction-list.fxml");
    }
    private void updateChartData(String time, double price) {
        Platform.runLater(() -> {
            priceSeries.getData().add(new XYChart.Data<>(time, price));
            // Giới hạn số lượng điểm trên biểu đồ để tránh tràn bộ nhớ/UI lag (ví dụ: giữ 20 điểm mới nhất)
            if (priceSeries.getData().size() > 20) {
                priceSeries.getData().remove(0);
            }
        });
    }
    private void enableAutoBidUI(double maxPrice, double increment) {
        autoBidMaxLabel.setText(String.format("%,.0f VND", maxPrice));
        autoBidIncrementLabel.setText(String.format("%,.0f VND", increment));
        autoBidInfoPane.setVisible(true);
        autoBidInfoPane.setManaged(true);
        autoBidButton.setDisable(true);
    }
    private void disableAutoBidUI() {
        autoBidInfoPane.setVisible(false);
        autoBidInfoPane.setManaged(false);
        autoBidButton.setDisable(false);
    }
    private void setupNumericValidation() {
        bidAmountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                bidAmountField.setText(newValue.replaceAll("[^\\d]", ""));
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
}
