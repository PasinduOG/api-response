package io.github.pasinduog.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base abstract class for all custom API exceptions in the application.
 * <p>
 * Instead of throwing generic exceptions, developers should extend this class
 * to create specific business exceptions. This allows the {@link GlobalExceptionHandler}
 * to automatically capture the specific {@link HttpStatus} and message defined by the subclass.
 * </p>
 *
 * <h2>Example Usage:</h2>
 * <pre>
 * {@code
 * public class ResourceNotFoundException extends ApiException {
 *     public ResourceNotFoundException(String resource, Long id) {
 *         super(String.format("%s not found with ID: %d", resource, id), HttpStatus.NOT_FOUND);
 *     }
 * }
 * }
 * </pre>
 *
 * @author Pasindu OG
 * @version 2.0.1
 * @since 1.2.0
 * @see GlobalExceptionHandler
 */
@Getter
public abstract class ApiException extends RuntimeException {

    /**
     * The HTTP status associated with this specific exception.
     */
    private final HttpStatus status;

    /**
     * Constructs a new ApiException with a detailed message and specific HTTP status.
     *
     * @param message The detail message explaining the cause of the error.
     * @param status  The {@link HttpStatus} to be returned to the client.
     */
    protected ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}