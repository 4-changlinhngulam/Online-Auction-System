package com.auction.server;

import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.ItemDAO;
import com.auction.server.dao.UserDAO;
import com.auction.shared.model.entity.Auction;
import com.auction.shared.model.entity.Bidder;
import com.auction.shared.model.entity.Electronics;
import com.auction.shared.model.enums.AuctionStatus;

import java.time.LocalDateTime;

public class TestDB {
    public static void main(String[] args) {
        System.out.println(" ĐANG BẮT ĐẦU TEST KẾT NỐI CLOUD DATABASE...");

        try {
            // 1. Khởi tạo các DAO
            UserDAO userDAO = new UserDAO();
            ItemDAO itemDAO = new ItemDAO();
            AuctionDAO auctionDAO = new AuctionDAO();

            // 2. Test tạo User (Đóng vai Bidder)
            Bidder testUser = new Bidder();
            testUser.setId("U_TEST_01");
            testUser.setName("nguoi_dung_test");
            testUser.setPassword("123456");
            testUser.setEmail("test@gmail.com");

            System.out.println("-> Đang lưu User xuống Cloud...");
            userDAO.save(testUser);

            // 3. Test tạo Item (Đóng vai đồ điện tử)
            Electronics testItem = new Electronics();
            testItem.setId("ITEM_TEST_01");
            testItem.setName("Laptop Gaming RTX 4090 Test");
            testItem.setDescription("Máy tính dùng để test DB");
            testItem.setStartingPrice(5000.0);

            System.out.println("-> Đang lưu Item xuống Cloud...");
            itemDAO.save(testItem);

            // 4. Test tạo Phiên đấu giá (Auction)
            Auction testAuction = new Auction();
            testAuction.setId("AUC_TEST_01");
            testAuction.setItem(testItem);
            testAuction.setCurrentPrice(5000.0);
            testAuction.setStartTime(LocalDateTime.now());
            testAuction.setEndTime(LocalDateTime.now().plusDays(3));
            testAuction.setStatus(AuctionStatus.OPEN);

            System.out.println("-> Đang lưu Phiên đấu giá xuống Cloud...");
            auctionDAO.save(testAuction);

            System.out.println("\n TEST THÀNH CÔNG RỰC RỠ! KHÔNG CÓ LỖI XẢY RA.");
            System.out.println("Hãy mở MySQL Workbench lên để tận mắt xem dữ liệu nhé!");

        } catch (Exception e) {
            System.err.println("\n ÁI CHÀ, CÓ LỖI RỒI:");
            e.printStackTrace();
        }
    }
}