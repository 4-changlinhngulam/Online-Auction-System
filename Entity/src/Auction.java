import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * 1. Đại diện cho một phiên đấu giá cụ thể đang diễn ra, bao bọc lấy một `Item` (sản phẩm).
 * 2. Chịu trách nhiệm quản lý thời gian (startTime, endTime) và trạng thái của phiên (OPEN, RUNNING, FINISHED, PAID, CANCELED).
 * 3. Giữ tham chiếu đến người thắng cuộc hiện tại (`currentWinner`) và mức giá cao nhất (`currentPrice`).
 * 4. Hàm `handleNewBid` được đánh dấu `synchronized` để đảm bảo an toàn luồng (Thread-safe) trong trường hợp nhiều người cùng lúc đặt giá (Race Condition).
 */
public class Auction extends Entity {
    private Item item;
    private double currentPrice;
    private Bidder currentWinner;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // OPEN, RUNNING, FINISHED, PAID, CANCELED

    // Khởi tạo ArrayList để lưu lịch sử không bị lỗi null
    private List<BidTransaction> bidHistory = new ArrayList<>();
    
    public Auction() {}

    // Xử lý lượt đặt giá mới (sử dụng synchronized để tránh xung đột khi nhiều người cùng đặt giá)
    public synchronized boolean handleNewBid(BidTransaction bid) {
        // Cập nhật giá cao nhất, currentWinner và thêm vào lịch sử
        return true;
    }

    // Đóng mở phiên giao dịch
    public void startAuction() {}
    public void closeAuction() {}

    public String getAuctionId() { return this.getId(); }
    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}