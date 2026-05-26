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

// - Design Pattern: Singleton (Đảm bảo duy nhất 1 phiên bản quản lý đấu giá, tránh xung đột dữ liệu).
// - Design Pattern: Observer (AuctionManager đóng vai trò Subject, quản lý danh sách ClientHandler là các Observer để push thông báo real-time).
public class AuctionManager {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(AuctionManager.class.getName());
    private static volatile AuctionManager instance;

    private final Map<String, Auction> activeAuctions;

    private final List<BidObserver> observers;

    private final AuctionDAO auctionDAO;

    private final ScheduledExecutorService scheduler;

    private AuctionManager() {
        this.activeAuctions = new ConcurrentHashMap<>();
        this.observers = new ArrayList<>();
        this.auctionDAO = new AuctionDAO();
        this.scheduler = Executors.newScheduledThreadPool(5);
    }

    public static AuctionManager getInstance() {
        AuctionManager auctionManager = instance;
        // Double-checked locking giúp tăng hiệu năng (chỉ block luồng khi instance thực
        // sự null).
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

    public void init() {
        System.out.println("Đang khôi phục các phiên đấu giá từ Database...");
        List<Auction> openAuctions = auctionDAO.getOpenAuctions();
        for (Auction auction : openAuctions) {
            addAuction(auction);
            scheduleAuctionEnd(auction);
            System.out.println("- Đã khôi phục và tiếp tục đếm giờ cho phiên: " + auction.getId());
        }
    }

    public void endAuction(String auctionId) {
        Auction auction = activeAuctions.get(auctionId);
        if (auction != null) {
            auction.setStatus(AuctionStatus.FINISHED);
            try {
                auctionDAO.update(auction);
                notifyObservers(auction);
                System.out.println("Phiên đấu giá " + auctionId + " đã kết thúc!");
            } catch (Exception e) {
                e.printStackTrace();
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
                observer.update(updatedAuction.getItem(), updatedAuction.getCurrentPrice(), winnerId);
            } catch (Exception e) {
                LOGGER.log(java.util.logging.Level.SEVERE,
                        "Lỗi gửi thông báo cho 1 client, gỡ bỏ client: " + e.getMessage(), e);
                removeObserver(observer);
            }
        }
    }

    public void scheduleAuctionEnd(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = auction.getEndTime();
        long delayInMillis = Duration.between(now, endTime).toMillis();

        if (delayInMillis <= 0) {
            endAuction(auction.getId());
        } else {
            scheduler.schedule(() -> {
                System.out.println("Hệ thống tự động chốt phiên đấu giá: " + auction.getId());
                endAuction(auction.getId());
            }, delayInMillis, TimeUnit.MILLISECONDS);
        }
    }

    public Response processNewBid(String auctionId, String bidderId, double bidAmount) {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            return new Response(false, "Phiên đấu giá không tồn tại.", null);
        }

        // Bắt buộc dùng `synchronized (auction)` để ngăn chặn Race Condition khi 2
        // client đặt giá cùng 1 mili-giây.
        // Chỉ block phiên đấu giá hiện tại, không block toàn bộ AuctionManager -> Tối
        // ưu hiệu suất.
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
                auctionDAO.update(auction);

                notifyObservers(auction);

                System.out.println("User " + bidderId + " đặt giá thành công: $" + bidAmount);
                return new Response(true, "Đặt giá thành công!", null);

            } catch (DataPersistenceException | EntityNotFoundException e) {
                LOGGER.log(java.util.logging.Level.SEVERE, "Lỗi Database khi lưu Bid: " + e.getMessage(), e);
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
}
