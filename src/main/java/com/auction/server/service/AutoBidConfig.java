package com.auction.server.service;

import java.io.Serializable;

public class AutoBidConfig implements Serializable {
    private String bidderId;
    private double maxAmount;

    public AutoBidConfig(String bidderId, double maxAmount) {
        this.bidderId = bidderId;
        this.maxAmount = maxAmount;
    }

    public String getBidderId() {
        return bidderId;
    }

    public double getMaxAmount() {
        return maxAmount;
    }
}
