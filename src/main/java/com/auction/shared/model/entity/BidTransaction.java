package com.auction.shared.model.entity;

import java.time.LocalDateTime;

/**
 * 1. Lớp BidTransaction có nhiệm vụ ghi lại dấu vết (log) của từng lượt đặt giá
 * cụ thể.
 * 2. Lưu trữ thông tin chi tiết
 * 3. Lớp này đáp ứng yêu cầu vẽ lại biểu đồ giá theo thời gian và lưu trữ lịch
 * sử minh bạch cho hệ thống.
 * 4. ID tự động được sinh từ lớp cha (Entity) bao gồm Timestamp để đảm bảo tính
 * tuần tự.
 */
public class BidTransaction extends Entity {
    private String bidderId;
    private String auctionId;
    private double amount;
    private LocalDateTime timestamp;

    public BidTransaction() {
        super(); // Gọi constructor lớp cha để sinh ID tuần tự
        this.timestamp = LocalDateTime.now();
    }

    // Getters và Setters
    public String getBidderId() {
        return bidderId;
    }

    public void setBidderId(String bidderId) {
        this.bidderId = bidderId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void printInfo() {
        System.out.printf("Giao dịch [%s]: Phiên %s - User %s đã đặt mức giá $%.2f vào lúc %s%n",
                getId(), auctionId, bidderId, amount, timestamp);
    }
}
