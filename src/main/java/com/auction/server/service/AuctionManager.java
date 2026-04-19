package com.auction.server.service;
import com.auction.shared.model.entity.Auction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/** Singleton – Quản lý toàn bộ phiên đấu giá. Xử lý concurrency + Observer notify. */
public class AuctionManager {
    private static AuctionManager instance;
    private final Map<String, Auction> activeAuctions = new ConcurrentHashMap<>();
    private AuctionManager() {}
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
    // TODO: placeBid() – dùng synchronized / ReentrantLock
    // TODO: closeAuction() – xác định người thắng
    // TODO: notifyObservers(auctionId) – broadcast cho các client đang subscribe
    // TODO: scheduleAuctionEnd() – dùng ScheduledExecutorService
}
