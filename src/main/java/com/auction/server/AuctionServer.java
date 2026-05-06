package com.auction.server;

import com.auction.server.handler.ClientHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/** Entry point của Server. Lắng nghe kết nối và tạo ClientHandler cho mỗi client. */
public class AuctionServer {
    private static final int PORT = 9999;

    public static void main(String[] args) {
        System.out.println("Server khởi động tại port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client mới kết nối: " + socket.getRemoteSocketAddress());
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            System.err.println("Lỗi khởi động server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
