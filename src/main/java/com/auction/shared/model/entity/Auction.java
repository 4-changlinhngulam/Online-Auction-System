package com.auction.shared.model.entity;

import com.auction.shared.model.enums.*;

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
    private AuctionStatus status; // OPEN, RUNNING, FINISHED, PAID, CANCELED

    // Khởi tạo ArrayList để lưu lịch sử không bị lỗi null
    private List<BidTransaction> bidHistory = new ArrayList<>();

    private List<BidObserver> observers = new ArrayList<>();

    public void addObserver(BidObserver obs) { observers.add(obs); }

    private void notifyObservers() {
        for(BidObserver obs : observers) {
            obs.update(this.item, this.currentPrice, this.currentWinner.getId());
        }
    }
    
    public Auction() {}

    /**
     * Hàm xử lý đặt giá đồng thời.
     * Từ khóa 'synchronized' đảm bảo tại 1 thời điểm chỉ có 1 thread (1 lượt bid)
     * được phép chạy vào hàm này cho cùng một đối tượng Auction.
     */
    public synchronized boolean handleNewBid(Bidder bidder, double bidAmount) {
        //Kiểm tra trạng thái phiên (Chỉ cho phép bid khi đang RUNNING)
        if (!AuctionStatus.RUNNING.equals(this.status)) {
            System.out.println("Phiên đấu giá không ở trạng thái mở!");
            return false;
        }

        //Kiểm tra thời gian (Đề phòng độ trễ mạng khiến bid tới sau khi đã đóng)
        if (LocalDateTime.now().isAfter(endTime)) {
            System.out.println("Phiên đấu giá đã kết thúc!");
            this.status = AuctionStatus.FINISHED;
            return false;
        }

        if (bidAmount <= currentPrice) {
            System.out.println(" Giá đặt (" + bidAmount + ") phải lớn hơn giá hiện tại (" + currentPrice + ")!");
            return false;
        }

        //Cập nhật thông tin người thắng và giá mới
        this.currentPrice = bidAmount;
        this.currentWinner = bidder;

        //Tạo và lưu Transaction vào lịch sử
        BidTransaction transaction = new BidTransaction();
        transaction.setBidderId(bidder.getId());
        transaction.setAuctionId(this.getId());
        transaction.setAmount(bidAmount);
        transaction.setTimestamp(LocalDateTime.now());

        bidHistory.add(transaction);

        System.out.println(bidder.getUsername() + " đặt giá thành công: $" + bidAmount);

        notifyObservers();

        return true;
    }

    // Đóng mở phiên giao dịch
    public void startAuction() {}
    public void closeAuction() {}

    public String getAuctionId() { return this.getId(); }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public AuctionStatus getStatus() { return status; }
    public void setStatus(AuctionStatus status) { this.status = status; }
}
