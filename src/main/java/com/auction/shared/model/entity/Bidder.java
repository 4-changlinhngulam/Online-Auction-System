package com.auction.shared.model.entity;

import com.auction.shared.model.enums.UserRole;
import java.util.ArrayList;
import java.util.List;

public class Bidder extends User {

    private List<Item> watchlist = new ArrayList<>();

    public Bidder(String username, String password, String email) {
        super(username, password, UserRole.BIDDER, email);
    }

    public Bidder() {
        super();
    }



    @Override
    public String getRole() {
        return "BIDDER";
    }

    public double placeManualBid(double currentPrice, double bidAmount) {
        if (bidAmount <= currentPrice) {
            throw new IllegalArgumentException("Invalid bid");
        }
        return bidAmount;
    }

    public void watchItem(Item item) {
        if (!this.watchlist.contains(item)) {
            this.watchlist.add(item);
        }
    }



    public List<Item> getWatchlist() {
        return watchlist;
    }

    public void setWatchlist(List<Item> watchlist) {
        this.watchlist = watchlist;
    }
}
