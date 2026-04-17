package com.auction.server;
/** Entry point của Server. Lắng nghe kết nối và tạo ClientHandler cho mỗi client. */
public class AuctionServer {
    private static final int PORT = 9999;
    public static void main(String[] args) {
        System.out.println("Server khởi động tại port " + PORT);
        // TODO: Khởi tạo AuctionManager.getInstance()
        // TODO: while(true) { Socket s = serverSocket.accept(); new Thread(new ClientHandler(s)).start(); }
    }
}
