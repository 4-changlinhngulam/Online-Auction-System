import java.util.ArrayList;
import java.util.List;

/**
 * 1. Lớp Bidder đại diện cho người tham gia đấu giá, kế thừa từ lớp User.
 * 2. Cung cấp các tính năng đặc thù cho người mua như đặt giá thủ công (placeManualBid) hoặc cài đặt giá tự động (setupAutoBid).
 * 3. Danh sách `watchlist` giúp Bidder theo dõi các sản phẩm mình quan tâm.
 * 4. Chứa logic Observer pattern thông qua hàm `update()` để nhận thông báo realtime khi có biến động giá.
 */
public class Bidder extends User {
    // Khởi tạo ArrayList để tránh NullPointerException khi thêm item vào watchlist
    private List<Item> watchlist = new ArrayList<>();
    private double maxAutoBidAmount;
    private boolean isAutoBidEnabled;

    public Bidder() {}

    @Override
    public String getRole() {
        return "BIDDER";
    }

    @Override
    public void showMenu() {
        // Logic hiển thị menu cho Bidder
    }

    public void placeManualBid() {
        // Đặt giá thủ công
    }

    public void watchItem(Item item) {
        // Theo dõi một sản phẩm để nhận thông báo realtime
        this.watchlist.add(item);
    }

    public void setupAutoBid(double maxAmount) {
        // Bật/tắt đấu giá tự động
        this.maxAutoBidAmount = maxAmount;
        this.isAutoBidEnabled = true;
    }

    // Update price (Observer pattern)
    public void update(Item item, double newPrice, String lastBidderId) {
        // Cập nhật giá mới hiển thị cho user
    }

    private void processAutoBid(Item item, double currentPrice) {
        // Logic tự động đặt giá nếu có người trả cao hơn và chưa vượt quá maxAutoBidAmount
    }
}