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
import com.auction.shared.exception.AuctionClosedException;
import com.auction.shared.exception.InvalidBidException;
import com.auction.shared.model.entity.Auction;
import com.auction.shared.model.entity.BidObserver;
import com.auction.shared.model.entity.BidTransaction;
import com.auction.shared.model.entity.Bidder;
import com.auction.shared.model.enums.AuctionStatus;
import com.auction.shared.protocol.Response;


/** Singleton – Quản lý toàn bộ phiên đấu giá. Xử lý concurrency + Observer notify. */
public class AuctionManager {
    // 1. Singleton: Đảm bảo chỉ có 1 Manager duy nhất trên toàn Server
    private static AuctionManager instance;

    // 2. Lưu trữ các phiên đấu giá đang chạy.
    private final Map<String, Auction> activeAuctions;

    // 3. Danh sách các Client đang theo dõi đấu giá (Dùng cho Observer Pattern)
    private final List<BidObserver> observers;

    // 4. Kết nối với Database
    private final AuctionDAO auctionDAO;

    // Khởi tạo một bộ đếm giờ với luồng chạy ngầm (pool size = 5)
    private final ScheduledExecutorService scheduler;
    private AuctionManager() {
        this.activeAuctions = new ConcurrentHashMap<>();
        this.observers = new ArrayList<>();
        this.auctionDAO = new AuctionDAO();
        this.scheduler = Executors.newScheduledThreadPool(5);
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
    public void addAuction(Auction auction) {
        activeAuctions.put(auction.getId(), auction);
    }

    /**
     * Hàm xử lý ra giá. Từ khóa `synchronized` CỰC KỲ QUAN TRỌNG:
     * Ngăn chặn việc 2 người bấm "Ra giá" cùng 1 phần nghìn giây gây lỗi giá.
     */
    public synchronized boolean placeBid(String auctionId, Bidder bidder, double bidAmount)
            throws AuctionClosedException, InvalidBidException {

        Auction auction = activeAuctions.get(auctionId);

        if (auction == null) {
            throw new IllegalArgumentException("Không tìm thấy phiên đấu giá này!");
        }

        if (auction.getStatus() != AuctionStatus.OPEN) {
            throw new AuctionClosedException("Phiên đấu giá đã kết thúc. Bạn không thể ra giá nữa!");
        }

        if (bidAmount <= auction.getCurrentPrice()) {
            throw new InvalidBidException("Mức giá phải cao hơn giá hiện tại (" + auction.getCurrentPrice() + ")");
        }

        auction.setCurrentPrice(bidAmount);
        auction.setCurrentWinner(bidder);

        // Lưu trạng thái mới xuống Database ngay lập tức
        try {
            auctionDAO.update(auction);
            System.out.println("Người dùng " + bidder.getUsername() + " vừa ra giá " + bidAmount + " cho " + auction.getId());
        } catch (Exception e) {
            System.err.println("Lỗi khi lưu giá mới xuống DB!");
            e.printStackTrace();
            return false;
        }

        // Thông báo cho tất cả người dùng khác cập nhật màn hình
        notifyObservers(auction);
        return true;
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
    // OBSERVER PATTERN (CƠ CHẾ THÔNG BÁO PUSH)

    public void addObserver(BidObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(BidObserver observer) {
        observers.remove(observer);
    }

    // Hàm này sẽ lặp qua tất cả các Client đang kết nối và báo cho họ biết có giá mới
    private void notifyObservers(Auction updatedAuction) {
        for (BidObserver observer : observers) {
            String winnerId = "";
            if (updatedAuction.getCurrentWinner() != null) {
                winnerId = updatedAuction.getCurrentWinner().getId();
            }

            observer.update(updatedAuction.getItem(), updatedAuction.getCurrentPrice(), winnerId);
        }
    }

    /**
     * Lên lịch tự động kết thúc phiên đấu giá
     */
    public void scheduleAuctionEnd(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = auction.getEndTime();

        // Tính khoảng thời gian từ bây giờ đến lúc kết thúc
        long delayInMillis = Duration.between(now, endTime).toMillis();

        if (delayInMillis <= 0) {
            // Nếu thời gian kết thúc ở trong quá khứ, đóng phiên ngay lập tức
            endAuction(auction.getId());
        } else {
            // Đặt báo thức: Lệnh () -> endAuction(...) sẽ được chạy sau "delayInMillis"
            scheduler.schedule(() -> {
                System.out.println("Hệ thống tự động chốt phiên đấu giá: " + auction.getId());
                endAuction(auction.getId());
            }, delayInMillis, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Xử lý một lượt đặt giá mới (Real-time).
     * BẮT BUỘC dùng 'synchronized' để chặn nhiều người đặt giá cùng lúc.
     */
    public synchronized Response processNewBid(String auctionId, String bidderId, double bidAmount) {

        // 1. Lấy thông tin phiên đấu giá từ RAM
        Auction auction = activeAuctions.get(auctionId);

        // Kiểm tra phiên đấu giá có tồn tại trên RAM không
        if (auction == null) {
            return new Response(false, "Phiên đấu giá không tồn tại hoặc đã kết thúc.", null);
        }

        // Kiểm tra trạng thái phải là OPEN
        if (auction.getStatus() != AuctionStatus.OPEN) {
            return new Response(false, "Phiên đấu giá hiện không mở.", null);
        }

        // 2. Kiểm tra giá đặt: Phải cao hơn giá cao nhất hiện tại
        if (bidAmount <= auction.getCurrentPrice()) {
            return new Response(false, "Giá đặt phải lớn hơn giá hiện tại (" + auction.getCurrentPrice() + ").", null);
        }

        try {
            // 3. Cập nhật thông tin TRÊN RAM ngay lập tức
            auction.setCurrentPrice(bidAmount);

            // Cập nhật người thắng tạm thời
            Bidder tempWinner = new Bidder();
            tempWinner.setId(bidderId);
            auction.setCurrentWinner(tempWinner);

            // 4. Ghi sổ TRONG DATABASE
            BidTransaction transaction = new BidTransaction();
            transaction.setAuctionId(auctionId);
            transaction.setBidderId(bidderId);
            transaction.setAmount(bidAmount);

            BidTransactionDAO bidDao = new BidTransactionDAO();
            boolean isSaved = bidDao.save(transaction);

            if (!isSaved) {
                return new Response(false, "Lỗi khi ghi nhận giao dịch vào cơ sở dữ liệu.", null);
            }

            // 5. Cập nhật giá mới của phiên đấu giá vào bảng 'auctions'
            auctionDAO.update(auction);

            notifyObservers(auction);

            return new Response(true, "Đặt giá thành công!", null);

        } catch (Exception e) {
            System.err.println("Lỗi Server khi xử lý Bid: " + e.getMessage());
            return new Response(false, "Lỗi máy chủ khi xử lý đặt giá.", null);
        }
    }
}

