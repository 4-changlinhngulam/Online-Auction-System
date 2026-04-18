import java.util.ArrayList;
import java.util.List;

/**
 * 1. Lớp Bidder đại diện cho người tham gia đấu giá, kế thừa từ lớp User.
 * 2. Cung cấp các tính năng đặc thù cho người mua như đặt giá thủ công (placeManualBid) hoặc cài đặt giá tự động (setupAutoBid).
 * 3. Danh sách `watchlist` giúp Bidder theo dõi các sản phẩm mình quan tâm.
 * 4. Chứa logic Observer pattern thông qua hàm `update()` để nhận thông báo realtime khi có biến động giá.
 */
public class Bidder extends User implements BidObserver {
    // Khởi tạo ArrayList để tránh NullPointerException khi thêm item vào watchlist
    private List<Item> watchlist = new ArrayList<>();
    private double maxAutoBidAmount;
    private boolean isAutoBidEnabled;

    public Bidder(String username) { super(username); }

    // Update price (Observer pattern)
    @Override
    public void update(Item item, double newPrice, String lastBidderId) {
        // 1. Cập nhật giao diện / in thông báo
        System.out.println("[" + this.getUsername() + " nhận thông báo]: '"
                + item.getName() + "' vừa lên mức giá " + newPrice);

        // 2. Nếu đang bật Auto-bid và người vừa đặt giá KHÔNG PHẢI là mình
        if (this.isAutoBidEnabled && !this.getId().equals(lastBidderId)) {
            // Tự động nhảy vào vòng lặp đấu giá tự động
            processAutoBid(item, newPrice);
        }
    }


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


    private void processAutoBid(Item item, double currentPrice) {
        // Logic auto-bid ...
    }
}