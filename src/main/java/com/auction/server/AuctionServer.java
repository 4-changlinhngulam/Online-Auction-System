package com.auction.server;

import com.auction.server.handler.ClientHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.auction.server.service.AuctionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lớp chính chịu trách nhiệm khởi chạy Server. Lắng nghe kết nối và tạo ClientHandler cho mỗi client bằng Thread Pool.
 */
public class AuctionServer {
    private static final Logger LOGGER = Logger.getLogger(AuctionServer.class.getName());
    private static final int PORT = 9999;
    private static final int MAX_CLIENTS = 200;
    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(MAX_CLIENTS);

    public static void main(String[] args) {
        LOGGER.info("Server đang khởi động...");

        // Nạp lại các phiên đấu giá đang mở từ Database để tiếp tục đếm giờ
        AuctionManager.getInstance().init();

        // Thêm Shutdown Hook để dọn dẹp Thread Pool khi tắt Server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Đang tắt Server, dọn dẹp Thread Pool...");
            THREAD_POOL.shutdown();
            try {
                if (!THREAD_POOL.awaitTermination(5, TimeUnit.SECONDS)) {
                    THREAD_POOL.shutdownNow();
                }
            } catch (InterruptedException e) {
                THREAD_POOL.shutdownNow();
            }
        }));

        LOGGER.info("Server đã sẵn sàng tại port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                LOGGER.info("Client mới kết nối: " + socket.getRemoteSocketAddress());
                THREAD_POOL.submit(new ClientHandler(socket));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khởi động server: " + e.getMessage(), e);
        }
    }
}
