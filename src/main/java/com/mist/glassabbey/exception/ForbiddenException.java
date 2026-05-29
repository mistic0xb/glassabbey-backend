package com.mist.glassabbey.exception;

public class ForbiddenException extends IllegalStateException {
    public ForbiddenException(String message) {
        super(message);
    }
}
