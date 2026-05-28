package com.auction.shared.model.entity;

import com.auction.shared.model.enums.AuctionStatus;
import com.auction.shared.model.enums.UserRole;
import java.util.List;

public class Admin extends User {

    public Admin(String username, String password, String email) {
        super(username, password, UserRole.ADMIN, email);
    }

    public Admin() {
        super();
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    public void banUser(User targetUser) {
        if (targetUser != null && !(targetUser instanceof Admin)) {
            System.out.println("User " + targetUser.getUsername() + " has been restricted.");
        }
    }

    public void moderateItem(Item item, boolean approve) {
        if (item != null && "PENDING".equals(item.getStatus())) {
            item.setStatus(approve ? "APPROVED" : "REJECTED");
        }
    }

    public void removeInvalidAuction(Auction auction) {
        if (auction != null && auction.getStatus() != AuctionStatus.FINISHED) {
            auction.setStatus(AuctionStatus.CANCELED);
        }
    }

    public void reviewAllTransactions(List<BidTransaction> transactions) {
        if (transactions != null && !transactions.isEmpty()) {
            for (BidTransaction tx : transactions) {
                tx.printInfo();
            }
        }
    }
}
