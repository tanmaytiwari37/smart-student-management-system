package com.vityarthi.sms.exception;

/**
 * Exception thrown when a requested student cannot be located in the repository.
 */
public class StudentNotFoundException extends Exception {
    public StudentNotFoundException(String message) {
        super(message);
    }
}
