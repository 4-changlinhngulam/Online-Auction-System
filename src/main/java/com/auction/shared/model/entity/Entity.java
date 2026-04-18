package com.auction.shared.model.entity;

import java.time.LocalDateTime;

/**
 * 1. Lớp Entity là một Abstract Class (lớp trừu tượng) đóng vai trò là lớp cha (Base Class) cho toàn bộ hệ thống.
 * 2. Mục đích của lớp này là gom nhóm các thuộc tính dùng chung (id, createdAt) để tái sử dụng code, tránh lặp lại ở các lớp con.
 * 3. Các thực thể chính trong hệ thống như User, Item, Auction, và BidTransaction đều sẽ kế thừa từ lớp này.
 */
public abstract class Entity {
    private String id;
    private LocalDateTime createdAt;

    public Entity() {
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
