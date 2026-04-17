package com.auction.shared.protocol;
import java.io.Serializable;
/** Gửi từ Client → Server qua Socket. */
public class Request implements Serializable {
    private static final long serialVersionUID = 1L;
    private RequestType type;
    private Object payload;
    private String token;
    public Request(RequestType type, Object payload) { this.type=type; this.payload=payload; }
    public RequestType getType() { return type; }
    public Object getPayload() { return payload; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token=token; }
}
