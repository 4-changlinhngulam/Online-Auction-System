import java.util.List;
import java.util.ArrayList;
// Singleton AuctionManager
public class AuctionManager {
    private static volatile AuctionManager instance;
    private List<Auction> activeAuctions;

    // Constructor private để ngăn khởi tạo từ bên ngoài
    private AuctionManager() {
        activeAuctions = new ArrayList<>();
    }

    // Phương thức public static để lấy thể hiện duy nhất (Double-checked locking)
    public static AuctionManager getInstance() {
        AuctionManager auctionManager = instance; // lấy dữ liệu instance vào biến cục bộ để tối ưu bộ nhớ
        if (auctionManager == null) {
            synchronized (AuctionManager.class) {
                auctionManager = instance;
                if (auctionManager == null) {
                    instance = auctionManager = new AuctionManager();
                }
            }
        }
        return auctionManager;
    }
    // Khởi tạo đối tượng auction và thêm vào activeAuctions
    public void createAuction(Auction auction) {
        activeAuctions.add(auction);
    }
}