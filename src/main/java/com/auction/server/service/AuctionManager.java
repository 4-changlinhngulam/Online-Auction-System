package com.auction.server.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.UserDAO;
import com.auction.shared.exception.DataPersistenceException;
import com.auction.shared.exception.EntityNotFoundException;
import com.auction.shared.model.entity.Auction;
import com.auction.shared.model.entity.BidObserver;
import com.auction.shared.model.entity.BidTransaction;
import com.auction.shared.model.entity.Bidder;
import com.auction.shared.model.entity.User;
import com.auction.shared.model.enums.AuctionStatus;
import com.auction.shared.protocol.Response;

// - Design Pattern: Singleton
// - Design Pattern: Observer
public class AuctionManager {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(AuctionManager.class.getName());
    private static final double MIN_INCREMENT = 50000;

    private static volatile AuctionManager instance;

    private final Map<String, Auction> activeAuctions;
    private final Map<String, List<AutoBidConfig>> autoBids;
    private final Map<String, java.util.concurrent.ScheduledFuture<?>> auctionTimers;
    private final List<BidObserver> observers;
    private final AuctionDAO auctionDAO;
    private final ScheduledExecutorService scheduler;

    private AuctionManager() {
        this.activeAuctions = new ConcurrentHashMap<>();
        this.autoBids = new ConcurrentHashMap<>();
        this.auctionTimers = new ConcurrentHashMap<>();
        this.observers = new ArrayList<>();
        this.auctionDAO = new AuctionDAO();
        this.scheduler = Executors.newScheduledThreadPool(5);
    }

    public static AuctionManager getInstance() {
        AuctionManager auctionManager = instance;
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

    public void addAuction(Auction auction) {
        activeAuctions.put(auction.getId(), auction);
    }

    public Response registerAutoBid(String auctionId, String bidderId, double maxAmount) {
        if (!activeAuctions.containsKey(auctionId)) {
            return new Response(false, "Phiên đấu giá không tồn tại.", null);
        }

        List<AutoBidConfig> configs = autoBids.computeIfAbsent(auctionId, k -> new ArrayList<>());

        synchronized (configs) {
            configs.removeIf(config -> config.getBidderId().equals(bidderId));
            configs.add(new AutoBidConfig(bidderId, maxAmount));
        }

        return new Response(true, "Cài đặt Auto-bid thành công.", null);
    }

    public void init() {
        LOGGER.info("Đang khôi phục các phiên đấu giá từ Database...");
        List<Auction> openAuctions = auctionDAO.getOpenAuctions();
        for (Auction auction : openAuctions) {
            addAuction(auction);
            if (auction.getStatus() == com.auction.shared.model.enums.AuctionStatus.RUNNING) {
                scheduleAuctionEnd(auction);
            }
            LOGGER.info("- Đã khôi phục phiên: " + auction.getId() + " (Trạng thái: " + auction.getStatus() + ")");
        }
    }

    public void endAuction(String auctionId) {
        Auction auction = activeAuctions.get(auctionId);
        if (auction != null) {
            auction.setStatus(AuctionStatus.FINISHED);
            try {
                auctionDAO.update(auction);
                notifyObservers(auction);
                LOGGER.info("Phiên đấu giá " + auctionId + " đã kết thúc!");
                activeAuctions.remove(auctionId);
                autoBids.remove(auctionId);
                auctionTimers.remove(auctionId);

            } catch (Exception e) {
                LOGGER.log(java.util.logging.Level.SEVERE, "Lỗi khi kết thúc phiên đấu giá: " + e.getMessage(), e);
            }
        }
    }

    public void addObserver(BidObserver observer) {
        if (observer != null) {
            synchronized (observers) {
                if (!observers.contains(observer)) {
                    observers.add(observer);
                }
            }
        }
    }

    public void removeObserver(BidObserver observer) {
        if (observer != null) {
            synchronized (observers) {
                observers.remove(observer);
            }
        }
    }

    private void notifyObservers(Auction updatedAuction) {
        String winnerId = (updatedAuction.getCurrentWinner() != null)
                ? updatedAuction.getCurrentWinner().getId()
                : "";

        List<BidObserver> copyList;
        synchronized (observers) {
            copyList = new ArrayList<>(observers);
        }

        for (BidObserver observer : copyList) {
            try {
                observer.update(
                        updatedAuction.getItem(),
                        updatedAuction.getCurrentPrice(),
                        winnerId,
                        updatedAuction.getEndTime());
            } catch (Exception e) {
                LOGGER.log(java.util.logging.Level.SEVERE,
                        "Lỗi gửi thông báo cho 1 client, gỡ bỏ client: " + e.getMessage(), e);
                removeObserver(observer);
            }
        }
    }

    public void scheduleAuctionEnd(Auction auction) {
        LocalDateTime endTime = auction.getEndTime();

        // Nếu end_time bị null trong DB → kết thúc phiên ngay lập tức thay vì crash
        if (endTime == null) {
            LOGGER.warning("Phiên đấu giá " + auction.getId()
                    + " không có end_time, tự động kết thúc ngay.");
            endAuction(auction.getId());
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        long delayInMillis = Duration.between(now, endTime).toMillis();

        if (delayInMillis <= 0) {
            endAuction(auction.getId());
        } else {
            java.util.concurrent.ScheduledFuture<?> existingTimer = auctionTimers.get(auction.getId());
            if (existingTimer != null && !existingTimer.isDone()) {
                existingTimer.cancel(false);
            }

            java.util.concurrent.ScheduledFuture<?> newTimer = scheduler.schedule(() -> {
                LOGGER.info("Hệ thống tự động chốt phiên đấu giá: " + auction.getId());
                endAuction(auction.getId());
            }, delayInMillis, TimeUnit.MILLISECONDS);

            auctionTimers.put(auction.getId(), newTimer);
        }
    }

    public Response processNewBid(String auctionId, String bidderId, double bidAmount) {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            return new Response(false, "Phiên đấu giá không tồn tại.", null);
        }

        synchronized (auction) {
            Bidder bidder;
            try {
                UserDAO userDAO = new UserDAO();
                User user = userDAO.findById(bidderId);
                if (!(user instanceof Bidder)) {
                    return new Response(false, "Tài khoản không có quyền đặt giá.", null);
                }
                bidder = (Bidder) user;
            } catch (EntityNotFoundException | DataPersistenceException e) {
                return new Response(false, "Lỗi xác thực người dùng: " + e.getMessage(), null);
            }

            boolean isValidBid = auction.handleNewBid(bidder, bidAmount);
            if (!isValidBid) {
                return new Response(false, "Mức giá không hợp lệ hoặc phiên đã kết thúc.", null);
            }

            try {
                BidTransaction transaction = new BidTransaction();
                transaction.setAuctionId(auctionId);
                transaction.setBidderId(bidderId);
                transaction.setAmount(bidAmount);

                BidTransactionDAO bidDao = new BidTransactionDAO();
                bidDao.save(transaction);

                // Anti-sniping: gia hạn thêm 3 phút nếu bid trong 3 phút cuối
                if (auction.getEndTime() != null) {
                    long remainingSeconds = Duration
                            .between(LocalDateTime.now(), auction.getEndTime()).getSeconds();
                    if (remainingSeconds >= 0 && remainingSeconds <= 180) {
                        auction.setEndTime(auction.getEndTime().plusSeconds(180));
                        scheduleAuctionEnd(auction);
                        LOGGER.info("Gia hạn phiên " + auctionId + " thêm 3 phút.");
                    }
                }

                auctionDAO.update(auction);
                notifyObservers(auction);

                LOGGER.info("User " + bidderId + " đặt giá thành công: $" + bidAmount);

                triggerAutoBids(auctionId, bidAmount, bidderId);

                return new Response(true, "Đặt giá thành công!", null);

            } catch (DataPersistenceException | EntityNotFoundException e) {
                LOGGER.log(java.util.logging.Level.SEVERE, "Lỗi Database khi lưu Bid: " + e.getMessage(), e);

                // Rollback giá về trước khi bid thất bại
                List<BidTransaction> history = auction.getBidHistory();
                if (history != null && !history.isEmpty()) {
                    history.remove(history.size() - 1);
                    if (!history.isEmpty()) {
                        BidTransaction prev = history.get(history.size() - 1);
                        auction.setCurrentPrice(prev.getAmount());
                        Bidder prevWinner = new Bidder();
                        prevWinner.setId(prev.getBidderId());
                        auction.setCurrentWinner(prevWinner);
                    } else {
                        auction.setCurrentPrice(auction.getItem().getStartingPrice());
                        auction.setCurrentWinner(null);
                    }
                }
                return new Response(false, "Lỗi máy chủ khi xử lý đặt giá.", null);
            }
        }
    }

    private void triggerAutoBids(String auctionId, double currentPrice, String lastBidderId) {
        List<AutoBidConfig> configs = autoBids.get(auctionId);
        if (configs == null || configs.isEmpty()) {
            return;
        }

        AutoBidConfig bestAutoBid = null;

        synchronized (configs) {
            for (AutoBidConfig config : configs) {
                if (config.getBidderId().equals(lastBidderId)) {
                    continue; // Không tự bid đè lên chính mình
                }

                double nextBid = currentPrice + MIN_INCREMENT;
                if (nextBid <= config.getMaxAmount()) {
                    if (bestAutoBid == null || config.getMaxAmount() > bestAutoBid.getMaxAmount()) {
                        bestAutoBid = config;
                    }
                }
            }
        }

        if (bestAutoBid != null) {
            final String bidderToAutoBid = bestAutoBid.getBidderId();
            final double nextBid = currentPrice + MIN_INCREMENT;
            LOGGER.info("Hệ thống Auto-bid cho " + bidderToAutoBid + " -> " + nextBid);

            // Chạy bất đồng bộ để tránh đệ quy và deadlock
            scheduler.execute(() -> processNewBid(auctionId, bidderToAutoBid, nextBid));
        }
    }
}
