package com.auction.client.network;
import com.auction.shared.protocol.Request;
import com.auction.shared.protocol.Response;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import javafx.application.Platform;

/** Singleton – Kết nối Socket duy nhất giữa Client và Server. */
public class ServerConnection {
    private static ServerConnection instance;
    private static final String HOST = "localhost";
    private static final int PORT = 9999;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    // Hàng đợi lưu trữ các Response trả lời cho Request thông thường
    private final BlockingQueue<Response> responseQueue = new LinkedBlockingQueue<>();
    private Thread listenerThread;
    
    // Hàm callback dùng để xử lý thông báo đẩy từ server (Push Notification)
    private Consumer<Response> pushNotificationListener;

    private ServerConnection() {}

    public static synchronized ServerConnection getInstance() {
        if (instance == null) instance = new ServerConnection();
        return instance;
    }

    public void connect() throws Exception {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            return;
        }
        socket = new Socket(HOST, PORT);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());

        // Bắt đầu luồng chạy ngầm để liên tục lắng nghe dữ liệu từ Server
        startListenerThread();
    }

    private void startListenerThread() {
        listenerThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Response res = (Response) in.readObject();
                    
                    // Nếu là thông báo đẩy (Push Notification), xử lý riêng
                    if ("NOTIFICATION_NEW_BID".equals(res.getMessage())) {
                        if (pushNotificationListener != null) {
                            Platform.runLater(() -> pushNotificationListener.accept(res));
                        }
                    } else {
                        // Nếu là Response trả lời bình thường, đưa vào hàng đợi
                        responseQueue.offer(res);
                    }
                }
            } catch (Exception e) {
                System.err.println("Mất kết nối tới Server: " + e.getMessage());
                disconnect();
            }
        });
        listenerThread.setDaemon(true); // Tự động tắt khi đóng ứng dụng
        listenerThread.start();
    }

    public void setPushNotificationListener(Consumer<Response> listener) {
        this.pushNotificationListener = listener;
    }

    // Hàm gửi request kiểu đồng bộ (vẫn khóa luồng UI nếu gọi trực tiếp)
    public synchronized Response sendRequest(Request request) throws Exception {
        if (socket == null || socket.isClosed()) {
            throw new IllegalStateException("Chưa kết nối với server");
        }
        out.writeObject(request);
        out.flush();
        // Thay vì in.readObject() (sẽ bị đụng luồng với Listener), ta lấy từ hàng đợi
        return responseQueue.take();
    }

    // Hàm gửi request kiểu BẤT ĐỒNG BỘ (An toàn cho JavaFX UI)
    public void sendRequestAsync(Request request, Consumer<Response> callback) {
        new Thread(() -> {
            try {
                Response res;
                // Khóa lại để đảm bảo mỗi Request đi kèm với đúng Response của nó
                synchronized (this) {
                    out.writeObject(request);
                    out.flush();
                    res = responseQueue.take();
                }
                if (callback != null) {
                    Platform.runLater(() -> callback.accept(res));
                }
            } catch (Exception e) {
                if (callback != null) {
                    Platform.runLater(() -> callback.accept(Response.error(e.getMessage())));
                }
            }
        }).start();
    }

    public void disconnect() {
        if (listenerThread != null) {
            listenerThread.interrupt();
            listenerThread = null;
        }
        try {
            if (out != null) out.close();
        } catch (Exception ignored) {
        }
        
        try {
            if (in != null) in.close();
        } catch (Exception ignored) {
        }
        
        try {
            if (socket != null) socket.close();
        } catch (Exception ignored) {
        }
        out = null;
        in = null;
        socket = null;
    }
}
