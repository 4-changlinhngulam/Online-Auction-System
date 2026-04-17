package com.auction.client.network;
import com.auction.shared.protocol.*;
import java.net.Socket;
/** Singleton – Kết nối Socket duy nhất giữa Client và Server. */
public class ServerConnection {
    private static ServerConnection instance;
    private static final String HOST = "localhost";
    private static final int PORT = 9999;
    private Socket socket;
    private ServerConnection() {}
    public static ServerConnection getInstance() {
        if (instance == null) instance = new ServerConnection();
        return instance;
    }
    public void connect() throws Exception {
        // TODO: socket = new Socket(HOST, PORT); mở ObjectInputStream / ObjectOutputStream
    }
    public Response sendRequest(Request request) throws Exception {
        // TODO: out.writeObject(request); return (Response) in.readObject();
        return null;
    }
    public void disconnect() { /* TODO: đóng socket */ }
}
