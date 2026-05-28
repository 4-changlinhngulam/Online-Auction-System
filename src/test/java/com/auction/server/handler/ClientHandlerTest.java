package com.auction.server.handler;

import com.auction.server.dao.DatabaseConnection;
import com.auction.server.dao.UserDAO;
import com.auction.shared.model.entity.Bidder;
import com.auction.shared.model.enums.UserStatus;
import com.auction.shared.protocol.Request;
import com.auction.shared.protocol.RequestType;
import com.auction.shared.protocol.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class ClientHandlerTest {

    private ServerSocket serverSocket;
    private int port;
    private Thread serverThread;
    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws Exception {
        userDAO = new UserDAO();

        // Khởi tạo cấu trúc bảng Database H2
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE, " +
                    "password VARCHAR(255), " +
                    "email VARCHAR(100), " +
                    "role VARCHAR(20), " +
                    "status VARCHAR(20))");
            stmt.execute("TRUNCATE TABLE users");
        }

        // Khởi động server socket trên một cổng ngẫu nhiên
        serverSocket = new ServerSocket(0);
        port = serverSocket.getLocalPort();

        serverThread = new Thread(() -> {
            try {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                handler.run();
            } catch (Exception e) {
                // Bỏ qua lỗi đóng socket khi dừng test
            }
        });
        serverThread.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (serverSocket != null) {
            serverSocket.close();
        }
        if (serverThread != null) {
            serverThread.interrupt();
        }
    }

    @Test
    @DisplayName("Test kết nối Socket và gửi yêu cầu đăng nhập qua ClientHandler")
    void testClientHandler_Login() throws Exception {
        // Chuẩn bị tài khoản người dùng trong DB với mật khẩu mã hóa BCrypt
        Bidder bidder = new Bidder("socket_user", BCrypt.hashpw("password123", BCrypt.gensalt()), "socket@gmail.com");
        bidder.setId("USR_SOCKET_01");
        bidder.setStatus(UserStatus.ACTIVE);
        userDAO.save(bidder);

        // Thiết lập kết nối client
        try (Socket clientSocket = new Socket("localhost", port);
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

            out.flush();

            // Gửi yêu cầu đăng nhập
            Request req = new Request(RequestType.LOGIN, new String[]{"socket_user", "password123"});
            out.writeObject(req);
            out.flush();

            // Đọc phản hồi từ Server
            Response res = (Response) in.readObject();

            assertNotNull(res);
            assertTrue(res.isSuccess());
            assertEquals("Đăng nhập thành công.", res.getMessage());
        }
    }
}
