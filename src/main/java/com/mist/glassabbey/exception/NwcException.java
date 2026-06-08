package com.mist.glassabbey.exception;

public class NwcException extends RuntimeException {
    public NwcException(String message) { super(message); }
    public NwcException(String message, Throwable cause) { super(message, cause); }
}
