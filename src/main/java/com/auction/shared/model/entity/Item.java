package com.auction.shared.model.entity;

/**
 * 1. Item là lớp quản lý thông tin sản phẩm mang ra đấu giá, kế thừa id và createdAt từ Entity.
 * 2. Lưu trữ trạng thái sản phẩm qua biến `status` (PENDING, APPROVED, REJECTED, SOLD)
 * để Admin kiểm duyệt trước khi đưa lên sàn.
 */
public abstract class Item extends Entity {
    private String name;
    private String description;
    private double startingPrice;
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, SOLD
    private String ownerId;
    private String ownerName;
    private byte[] imageBytes;
    private java.time.LocalDateTime preferredStartTime;
    private java.time.LocalDateTime preferredEndTime;

    // Constructor dùng chung cho Factory
    public Item(String id, String name, double startingPrice) {
        this.setId(id);
        this.name = name;
        this.startingPrice = startingPrice;
        this.status = "PENDING";
    }

    public Item() {
        // Constructor mặc định cho các thư viện mapping dữ liệu
    }

    // Getters & Setters (Encapsulation)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }

    public java.time.LocalDateTime getPreferredStartTime() {
        return preferredStartTime;
    }

    public void setPreferredStartTime(java.time.LocalDateTime preferredStartTime) {
        this.preferredStartTime = preferredStartTime;
    }

    public java.time.LocalDateTime getPreferredEndTime() {
        return preferredEndTime;
    }

    public void setPreferredEndTime(java.time.LocalDateTime preferredEndTime) {
        this.preferredEndTime = preferredEndTime;
    }

    public abstract void printInfo();
}
