package com.auction.shared.model.entity;
public interface BidObserver {
    void update(Item item, double newPrice, String lastBidderId);
}
