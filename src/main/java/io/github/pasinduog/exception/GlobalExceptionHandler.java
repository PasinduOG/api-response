package io.github.pasinduog.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for the application.
 * <p>
 * This class intercepts exceptions thrown by any controller across the application
 * and transforms them into a standard {@link ProblemDetail} format (RFC 7807).
 * This ensures a consistent error response structure for API clients.
 * </p>
 * <p>
 * <b>Automatically registered</b> via Spring Boot auto-configuration when the library
 * is on the classpath. No manual configuration required.
 * </p>
 * <h2>Handled Exception Types:</h2>
 * <ul>
 *   <li>{@link Exception} - Catch-all for unexpected errors (HTTP 500)</li>
 *   <li>{@link MethodArgumentNotValidException} - Bean validation failures (HTTP 400)</li>
 *   <li>{@link MethodArgumentTypeMismatchException} - Type conversion errors (HTTP 400)</li>
 *   <li>{@link NullPointerException} - Null pointer errors (HTTP 500)</li>
 *   <li>{@link ApiException} - Custom business logic exceptions (custom HTTP status)</li>
 * </ul>
 *
 * @author Pasindu OG
 * @version 1.3.0
 * @since 1.1.0
 * @see ProblemDetail
 * @see ApiException
 */
@RestControllerAdvice
@Slf4j
@SuppressWarnings("unused")
public class GlobalExceptionHandler {

    /**
     * Handles all unexpected exceptions (catch-all handler).
     * <p>
     * This method acts as a safety net for any error not explicitly handled by other methods.
     * It returns a generic 500 Internal Server Error to avoid leaking sensitive stack traces
     * to the client, while logging the full error details for debugging.
     * </p>
     *
     * @param ex The exception that was thrown.
     * @return A {@link ProblemDetail} with HTTP 500 status and a generic error message.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAllExceptions(Exception ex) {
        log.error("An unexpected error occurred: ", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error. Please contact technical support");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles bean validation errors (e.g., @NotNull, @Size failures).
     * <p>
     * Triggers when a {@link org.springframework.web.bind.annotation.RequestBody} fails validation.
     * It collects all field-level errors and returns them in a map under the "errors" property.
     * If a field has multiple errors, they are joined by a semicolon.
     * </p>
     *
     * @param ex The validation exception containing the list of field errors.
     * @return A {@link ProblemDetail} with HTTP 400 status and a map of validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errorMessage = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            assert fieldError.getDefaultMessage() != null;
            // If multiple errors exist for the same field, merge them
            errorMessage.merge(fieldError.getField(), fieldError.getDefaultMessage(),
                    (msg1, msg2) -> msg1 + "; " + msg2);
        }
        log.warn("Validation error: {}", errorMessage);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation Failed");
        problemDetail.setProperty("errors", errorMessage);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles type mismatch errors for method arguments.
     * <p>
     * This occurs when a request parameter cannot be converted to the expected type,
     * such as passing a string where an integer is required.
     * </p>
     *
     * @param ex The type mismatch exception.
     * @return A {@link ProblemDetail} with HTTP 400 status and a descriptive error message.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatchException(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {
        String errorMessage = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s.",
                ex.getValue(), ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        log.warn("Type mismatch error: {}", errorMessage);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorMessage);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles NullPointerExceptions explicitly.
     * <p>
     * While strictly not necessary (as the catch-all handler covers this), having a specific
     * handler allows for distinct logging or custom messaging for NPEs if required in the future.
     * </p>
     *
     * @param ex The NullPointerException that was thrown.
     * @return A {@link ProblemDetail} with HTTP 500 status.
     */
    @ExceptionHandler(NullPointerException.class)
    public ProblemDetail handleNullPointerExceptions(NullPointerException ex) {
        log.error("Null pointer exception occurred: ", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "A null pointer exception occurred.");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles all custom exceptions that extend {@link ApiException}.
     * <p>
     * This method dynamically extracts the HTTP status and message from the thrown
     * exception, providing a standardized RFC 7807 response for any domain-specific
     * error created by the library user.
     * </p>
     *
     * @param ex The custom API exception instance.
     * @return A {@link ProblemDetail} with the status and message defined in the exception.
     */
    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException ex) {
        log.warn("Business logic exception: {} | Status: {}", ex.getMessage(), ex.getStatus());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }
}