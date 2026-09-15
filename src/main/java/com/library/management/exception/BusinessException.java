package com.library.management.exception;

/**
 * Thrown when a request is well-formed but violates a business rule,
 * e.g. issuing a book with zero available copies, or duplicate ISBN/email.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
