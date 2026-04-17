package com.auction.server.handler;
import com.auction.shared.protocol.*;
import java.net.Socket;
/** Xử lý một Client trong luồng riêng. Đọc Request → gọi Service → gửi Response. */
public class ClientHandler implements Runnable {
    private final Socket socket;
    public ClientHandler(Socket socket) { this.socket = socket; }
    @Override
    public void run() {
        // TODO: Mở ObjectInputStream/ObjectOutputStream
        // TODO: Vòng lặp: Request req = (Request) in.readObject(); Response res = dispatch(req); out.writeObject(res);
    }
    private Response dispatch(Request req) {
        // TODO: switch(req.getType()) → gọi service tương ứng
        return Response.error("Not implemented");
    }
}
