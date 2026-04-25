package com.auction.shared.exception;

public class UnauthorizedActionException extends RuntimeException {
    public UnauthorizedActionException(String msg) {
        super(msg);
    }
}
