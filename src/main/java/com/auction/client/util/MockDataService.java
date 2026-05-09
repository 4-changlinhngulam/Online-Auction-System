package com.auction.client.util;

import com.auction.shared.model.entity.*;
import com.auction.shared.model.enums.AuctionStatus;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Dữ liệu giả để test GUI (JavaFX) độc lập, không cần kết nối Server.
 * Giúp thiết kế giao diện nhanh chóng hơn.
 */
public class MockDataService {

    public static List<Auction> getFakeAuctions() {
        List<Auction> list = new ArrayList<>();

        // 1. Tạo item giả bằng Constructor chuẩn (id, name, startingPrice)
        Electronics item1 = new Electronics("ITEM_01", "iPhone 15 Pro", 15000000);
        item1.setDescription("Điện thoại Apple mới nhất");
        item1.setWarrantyMonths(12);

        Art item2 = new Art("ITEM_02", "Tranh Sơn Dầu", 5000000);
        item2.setDescription("Tác phẩm nghệ thuật độc đáo");

        // 2. Tạo auction giả bằng Constructor rỗng và dùng Setter
        Auction a1 = new Auction();
        a1.setId("AUC_01");
        a1.setItem(item1);
        a1.setCurrentPrice(15000000);
        a1.setStartTime(LocalDateTime.now());
        a1.setEndTime(LocalDateTime.now().plusHours(2));
        a1.setStatus(AuctionStatus.RUNNING);

        Auction a2 = new Auction();
        a2.setId("AUC_02");
        a2.setItem(item2);
        a2.setCurrentPrice(5000000);
        a2.setStartTime(LocalDateTime.now());
        a2.setEndTime(LocalDateTime.now().plusHours(5));
        a2.setStatus(AuctionStatus.OPEN);

        list.add(a1);
        list.add(a2);
        return list;
    }

    public static List<BidTransaction> getFakeBidHistory(String auctionId) {
        List<BidTransaction> history = new ArrayList<>();

        // Dùng Setter để đổ dữ liệu thay vì nhét thẳng vào Constructor
        BidTransaction b1 = new BidTransaction();
        b1.setAuctionId(auctionId);
        b1.setBidderId("bidder-1");
        b1.setAmount(15500000);

        BidTransaction b2 = new BidTransaction();
        b2.setAuctionId(auctionId);
        b2.setBidderId("bidder-2");
        b2.setAmount(16000000);

        BidTransaction b3 = new BidTransaction();
        b3.setAuctionId(auctionId);
        b3.setBidderId("bidder-1");
        b3.setAmount(17000000);

        history.add(b1);
        history.add(b2);
        history.add(b3);
        return history;
    }

    public static User getFakeUser() {
        // Constructor của Bidder chỉ nhận 3 tham số: username, password, email
        Bidder user = new Bidder("testuser", "123", "test@mail.com");
        user.setId("USER_01");
        return user;
    }
}