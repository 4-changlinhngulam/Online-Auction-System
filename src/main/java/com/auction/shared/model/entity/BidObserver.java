package com.auction.shared.model.entity;

import java.time.LocalDateTime;

public interface BidObserver {
    void update(Item item, double newPrice, String lastBidderId, LocalDateTime newEndTime);
}
