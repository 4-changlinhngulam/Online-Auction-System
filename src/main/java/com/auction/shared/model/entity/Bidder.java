package com.auction.shared.model.entity;

import com.auction.shared.model.enums.UserRole;
import java.util.ArrayList;
import java.util.List;

public class Bidder extends User implements BidObserver {

    // Quy định bước giá cố định của sàn
    private static final double MIN_INCREMENT = 50000;

    private List<Item> watchlist = new ArrayList<>();
    private double maxAutoBidAmount;
    private boolean isAutoBidEnabled;

    public Bidder(String username, String password, String email) {
        super(username, password, UserRole.BIDDER, email);
    }

    public Bidder() {
        super();
    }

    @Override
    public void update(Item item, double newPrice, String lastBidderId) {
        System.out.println("[" + this.getUsername() + "]: '" + item.getName() + "' -> " + newPrice);

        // Kích hoạt auto-bid nếu đang bật và người vừa đặt không phải là mình
        if (this.isAutoBidEnabled && !this.getId().equals(lastBidderId)) {
            processAutoBid(item, newPrice);
        }
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

    // Hàm setup chỉ còn nhận vào giá trần (maxAmount)
    public void setupAutoBid(double maxAmount) {
        this.maxAutoBidAmount = maxAmount;
        this.isAutoBidEnabled = true;
    }

    private void processAutoBid(Item item, double currentPrice) {
        // Tự động cộng thêm mức tiền quy định của sàn
        double nextBid = currentPrice + MIN_INCREMENT;

        if (nextBid <= this.maxAutoBidAmount) {
            System.out.println(this.getUsername() + " -> " + nextBid);
            // TODO: Gửi request đặt giá lên Server tại đây
        } else {
            // Vượt quá ngân sách -> Tự động tắt auto-bid
            this.isAutoBidEnabled = false;
        }
    }

    public double getMaxAutoBidAmount() {
        return maxAutoBidAmount;
    }

    public void setMaxAutoBidAmount(double maxAutoBidAmount) {
        this.maxAutoBidAmount = maxAutoBidAmount;
    }

    public boolean isAutoBidEnabled() {
        return isAutoBidEnabled;
    }

    public void setAutoBidEnabled(boolean autoBidEnabled) {
        this.isAutoBidEnabled = autoBidEnabled;
    }

    public List<Item> getWatchlist() {
        return watchlist;
    }

    public void setWatchlist(List<Item> watchlist) {
        this.watchlist = watchlist;
    }
}
