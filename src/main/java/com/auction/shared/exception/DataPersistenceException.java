package com.auction.shared.exception;

public class DataPersistenceException extends RuntimeException {
    public DataPersistenceException(String msg) {
        super(msg);
    }

    public DataPersistenceException(String msg, Throwable cause) {
        super(msg, cause);
    }
}