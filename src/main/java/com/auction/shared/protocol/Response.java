package com.auction.shared.protocol;
import java.io.Serializable;
/** Gửi từ Server → Client. */
public class Response implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private String message;
    private Object data;
    public Response(boolean success, String message, Object data) { this.success=success; this.message=message; this.data=data; }
    public static Response ok(Object data) { return new Response(true,"OK",data); }
    public static Response error(String msg) { return new Response(false,msg,null); }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}
