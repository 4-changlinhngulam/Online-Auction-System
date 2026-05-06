package com.auction.server.handler;

import com.auction.shared.protocol.Request;
import com.auction.shared.protocol.RequestType;
import com.auction.shared.protocol.Response;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/** Xử lý một Client trong luồng riêng. Đọc Request → gọi Service → gửi Response. */
public class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (Socket ignored = this.socket;
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.flush();
            while (true) {
                Request req = (Request) in.readObject();
                Response res = dispatch(req);
                out.writeObject(res);
                out.flush();
            }
        } catch (EOFException eof) {
            System.out.println("Kết nối client đã đóng: " + socket.getRemoteSocketAddress());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Lỗi xử lý client: " + e.getMessage());
        }
    }

    private Response dispatch(Request req) {
        if (req == null || req.getType() == null) {
            return Response.error("Request không hợp lệ");
        }
        RequestType type = req.getType();
        switch (type) {
            case LOGIN:
            case REGISTER:
            case LOGOUT:
            case GET_USER_PROFILE:
            case UPDATE_USER_PROFILE:
            case CREATE_ITEM:
            case GET_ITEM:
            case UPDATE_ITEM:
            case DELETE_ITEM:
            case GET_ALL_ITEMS:
            case SEARCH_ITEM:
            case CREATE_AUCTION:
            case GET_AUCTION:
            case GET_ALL_AUCTIONS:
            case PLACE_BID:
            case CLOSE_AUCTION:
            case GET_BID_HISTORY:
            case BAN_USER:
            case GET_ALL_USERS:
            case SUBSCRIBE_AUCTION:
                return Response.error("Chức năng server chưa được triển khai");
            default:
                return Response.error("RequestType không được hỗ trợ: " + type);
        }
    }
}
