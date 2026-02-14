package io.github.pasinduog.exception;

import org.springframework.http.HttpStatus;

/**
 * Base abstract exception class for API-related exceptions.
 * <p>
 * This class provides a foundation for creating custom business logic exceptions
 * with associated HTTP status codes. Extend this class to create specific
 * exception types for your application.
 * </p>
 *
 * @author Pasindu OG
 * @version 2.0.0
 * @since 1.0.0
 */
public abstract class ApiException extends RuntimeException {

    /**
     * The HTTP status code associated with this exception.
     */
    private final HttpStatus status;

    /**
     * Constructs a new ApiException with the specified message and status.
     *
     * @param message the detail message
     * @param status the HTTP status code
     */
    protected ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * Gets the HTTP status code associated with this exception.
     *
     * @return the HTTP status
     */
    public HttpStatus getStatus() {
        return status;
    }
}