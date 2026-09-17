package com.vityarthi.sms.exception;

/**
 * Exception thrown when business validation constraints fail on student inputs.
 */
public class InvalidDataException extends Exception {
    public InvalidDataException(String message) {
        super(message);
    }
}
