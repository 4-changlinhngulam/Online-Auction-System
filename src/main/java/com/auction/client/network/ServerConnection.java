package com.auction.client.network;
import com.auction.shared.protocol.Request;
import com.auction.shared.protocol.Response;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/** Singleton – Kết nối Socket duy nhất giữa Client và Server. */
public class ServerConnection {
    private static ServerConnection instance;
    private static final String HOST = "localhost";
    private static final int PORT = 9999;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

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
    }

    public synchronized Response sendRequest(Request request) throws Exception {
        if (socket == null || socket.isClosed()) {
            throw new IllegalStateException("Chưa kết nối với server");
        }
        out.writeObject(request);
        out.flush();
        Object response = in.readObject();
        return (Response) response;
    }

    public void disconnect() {
        try {
            if (out != null) out.close();
        } catch (Exception ignored) {}
        try {
            if (in != null) in.close();
        } catch (Exception ignored) {}
        try {
            if (socket != null) socket.close();
        } catch (Exception ignored) {}
        out = null;
        in = null;
        socket = null;
    }
}
