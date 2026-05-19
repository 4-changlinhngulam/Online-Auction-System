package com.auction.shared.model.entity;

import com.auction.shared.model.enums.AuctionStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Auction extends Entity {
    private Item item;
    private double currentPrice;
    private Bidder currentWinner;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionStatus status;
    private List<BidTransaction> bidHistory = new ArrayList<>();
    public Auction() {
    }



    public synchronized boolean handleNewBid(Bidder bidder, double bidAmount) {
        if (this.status != AuctionStatus.RUNNING) {
            return false;
        }

        if (this.endTime != null && LocalDateTime.now().isAfter(endTime)) {
            this.status = AuctionStatus.FINISHED;
            return false;
        }

        if (bidAmount <= this.currentPrice) {
            return false; // Giá không hợp lệ
        }

        // Cập nhật trạng thái RAM
        this.currentPrice = bidAmount;
        this.currentWinner = bidder;

        // Ghi log trên RAM
        BidTransaction transaction = new BidTransaction();
        transaction.setBidderId(bidder.getId());
        transaction.setAuctionId(this.getId());
        transaction.setAmount(bidAmount);
        transaction.setTimestamp(LocalDateTime.now());
        this.bidHistory.add(transaction);

        return true; // Trả về true nếu hợp lệ
    }

    public void startAuction() {
        if (this.status == AuctionStatus.OPEN) {
            this.status = AuctionStatus.RUNNING;
            this.startTime = LocalDateTime.now();
        }
    }

    public void closeAuction() {
        if (this.status == AuctionStatus.RUNNING) {
            this.status = AuctionStatus.FINISHED;
        }
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Bidder getCurrentWinner() {
        return currentWinner;
    }

    public void setCurrentWinner(Bidder currentWinner) {
        this.currentWinner = currentWinner;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<BidTransaction> getBidHistory() {
        return bidHistory;
    }

    public void setBidHistory(List<BidTransaction> bidHistory) {
        this.bidHistory = bidHistory;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }
}
