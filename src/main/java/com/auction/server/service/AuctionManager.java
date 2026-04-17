package com.auction.server.service;
import com.auction.shared.model.entity.Auction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/** Singleton – Quản lý toàn bộ phiên đấu giá. Xử lý concurrency + Observer notify. */
public class AuctionManager {
    private static AuctionManager instance;
    private final Map<String, Auction> activeAuctions = new ConcurrentHashMap<>();
    private AuctionManager() {}
    public static synchronized AuctionManager getInstance() {
        if (instance == null) instance = new AuctionManager();
        return instance;
    }
    // TODO: placeBid() – dùng synchronized / ReentrantLock
    // TODO: closeAuction() – xác định người thắng
    // TODO: notifyObservers(auctionId) – broadcast cho các client đang subscribe
    // TODO: scheduleAuctionEnd() – dùng ScheduledExecutorService
}
