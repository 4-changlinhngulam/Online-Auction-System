package com.auction.client;

import com.auction.shared.model.entity.Bidder;
import com.auction.shared.model.entity.Item;
import com.auction.shared.model.entity.Electronics;
import com.auction.shared.model.entity.Auction;
import com.auction.shared.protocol.Request;
import com.auction.shared.protocol.RequestType;
import com.auction.shared.protocol.Response;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class BidderIntegrationTest {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 9999);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            System.out.println("Đang kết nối tới Server...");
            System.out.println("Kết nối thành công!");

            // 1. Đăng ký Bidder
            Bidder newBidder = new Bidder();
            newBidder.setName("bidder_test");
            newBidder.setPassword("123456"); // Sẽ được băm
            newBidder.setEmail("bidder@test.com");
            newBidder.setRole(com.auction.shared.model.enums.UserRole.BIDDER);

            System.out.println("\n[1] Thử tạo tài khoản Bidder...");
            out.writeObject(new Request(RequestType.REGISTER, newBidder));
            out.flush();
            Response resReg = (Response) in.readObject();
            System.out.println("Kết quả Đăng ký: " + resReg.getMessage());

            // 2. Đăng nhập Bidder
            System.out.println("\n[2] Đăng nhập bằng tài khoản vừa tạo...");
            out.writeObject(new Request(RequestType.LOGIN, new String[]{"bidder_test", "123456"}));
            out.flush();
            Response resLogin = (Response) in.readObject();
            System.out.println("Kết quả Đăng nhập: " + resLogin.getMessage());
            
            if (!resLogin.isSuccess()) {
                System.out.println("Không thể đăng nhập. Dừng test.");
                return;
            }
            Bidder loggedIn = (Bidder) resLogin.getData();

            // 3. Lấy danh sách Phiên đấu giá
            System.out.println("\n[3] Lấy danh sách phiên đấu giá...");
            out.writeObject(new Request(RequestType.GET_ALL_AUCTIONS, null));
            out.flush();
            Response resAuctions = (Response) in.readObject();
            System.out.println("Lấy danh sách thành công.");
            List<Auction> auctions = (List<Auction>) resAuctions.getData();
            
            if (auctions != null && !auctions.isEmpty()) {
                Auction auction = auctions.get(0);
                System.out.println("-> Tìm thấy phiên đấu giá: " + auction.getItem().getName() + " | Giá hiện tại: " + auction.getCurrentPrice());
                
                // 4. Đặt giá (Place Bid)
                double newBid = auction.getCurrentPrice() + 500000;
                System.out.println("\n[4] Tiến hành đặt giá: " + newBid + " VND");
                out.writeObject(new Request(RequestType.PLACE_BID, new Object[]{auction.getId(), loggedIn.getId(), newBid}));
                out.flush();
                Response resBid = (Response) in.readObject();
                System.out.println("Kết quả Đặt giá: " + resBid.getMessage());

                // 5. Đăng ký Auto Bid
                double maxAutoBid = newBid + 2000000;
                System.out.println("\n[5] Đăng ký Auto Bid với giá trần: " + maxAutoBid + " VND");
                out.writeObject(new Request(RequestType.SETUP_AUTO_BID, new Object[]{auction.getId(), loggedIn.getId(), maxAutoBid}));
                out.flush();
                Response resAuto = (Response) in.readObject();
                System.out.println("Kết quả Auto Bid: " + resAuto.getMessage());
            } else {
                System.out.println("-> Không có phiên đấu giá nào đang mở để test đặt giá.");
            }

            System.out.println("\nHoàn tất bài test Integration Bidder!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
