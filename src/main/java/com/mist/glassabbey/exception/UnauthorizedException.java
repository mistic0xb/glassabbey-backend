package com.mist.glassabbey.exception;

public class UnauthorizedException extends IllegalStateException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
