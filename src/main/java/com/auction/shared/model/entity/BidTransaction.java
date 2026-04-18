package com.auction.shared.model.entity;
import java.time.LocalDateTime;

/**
 * 1. Lớp BidTransaction có nhiệm vụ ghi lại dấu vết (log) của từng lượt đặt giá cụ thể.
 * 2. Lưu trữ thông tin chi tiết: Ai là người đặt (bidderId), đặt vào phiên nào (auctionId), mức giá bao nhiêu (amount), và chính xác thời điểm nào (timestamp).
 * 3. Lớp này đáp ứng yêu cầu vẽ lại biểu đồ giá theo thời gian và lưu trữ lịch sử minh bạch cho hệ thống.
 * 4. Phương thức static `generateId()` giúp tạo ra chuỗi định danh duy nhất (có thể sử dụng UUID) cho mỗi phiên giao dịch.
 */
public class BidTransaction extends Entity {
    private String bidderId;
    private String auctionId;
    private double amount;
    private LocalDateTime timestamp;

    public BidTransaction() {
        this.timestamp = LocalDateTime.now();
    }

    // Phương thức tĩnh để tạo ID ngẫu nhiên cho giao dịch
    private static String generateId() {
        return java.util.UUID.randomUUID().toString();
    }

    // Getters
    public String getBidderId() { return bidderId; }
    public void setBidderId(String bidderId) { this.bidderId = bidderId; }

    public String getAuctionId() { return auctionId; }
    public void setAuctionId(String auctionId) { this.auctionId = auctionId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public void printInfo() {
        // In chi tiết giao dịch
    }
}
