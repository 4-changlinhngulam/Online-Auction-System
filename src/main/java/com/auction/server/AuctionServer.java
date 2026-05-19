package com.auction.server;

import com.auction.server.handler.ClientHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.auction.server.service.AuctionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Entry point của Server. Lắng nghe kết nối và tạo ClientHandler cho mỗi client bằng Thread Pool.
 */
public class AuctionServer {
    private static final int PORT = 9999;
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        System.out.println("Server đang khởi động...");

        // Nạp lại các phiên đấu giá đang mở từ Database để tiếp tục đếm giờ
        AuctionManager.getInstance().init();

        // Thêm Shutdown Hook để dọn dẹp Thread Pool khi tắt Server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Đang tắt Server, dọn dẹp Thread Pool...");
            THREAD_POOL.shutdown();
            try {
                if (!THREAD_POOL.awaitTermination(5, TimeUnit.SECONDS)) {
                    THREAD_POOL.shutdownNow();
                }
            } catch (InterruptedException e) {
                THREAD_POOL.shutdownNow();
            }
        }));

        System.out.println("Server đã sẵn sàng tại port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client mới kết nối: " + socket.getRemoteSocketAddress());
                THREAD_POOL.submit(new ClientHandler(socket));
            }
        } catch (IOException e) {
            System.err.println("Lỗi khởi động server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
