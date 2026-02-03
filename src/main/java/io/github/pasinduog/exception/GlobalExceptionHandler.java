package io.github.pasinduog.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for the application.
 * <p>
 * This class intercepts exceptions thrown by any controller across the application
 * and transforms them into a standard {@link ProblemDetail} format (RFC 9457 / 7807).
 * This ensures a consistent error response structure for API clients.
 * </p>
 * <p>
 * <b>Automatically registered</b> via Spring Boot autoconfiguration when the library
 * is on the classpath. No manual configuration required.
 * </p>
 * <h2>Handled Exception Types:</h2>
 * <ul>
 * <li>{@link Exception} - Catch-all for unexpected errors (HTTP 500)</li>
 * <li>{@link MethodArgumentNotValidException} - Bean validation failures (HTTP 400)</li>
 * <li>{@link MethodArgumentTypeMismatchException} - Type conversion errors (HTTP 400)</li>
 * <li>{@link HttpMessageNotReadableException} - Malformed JSON body (HTTP 400)</li>
 * <li>{@link MissingServletRequestParameterException} - Missing required parameters (HTTP 400)</li>
 * <li>{@link NoResourceFoundException} - 404 Not Found for endpoints/resources (HTTP 404)</li>
 * <li>{@link HttpRequestMethodNotSupportedException} - Invalid HTTP method (HTTP 405)</li>
 * <li>{@link HttpMediaTypeNotSupportedException} - Unsupported Content-Type (HTTP 415)</li>
 * <li>{@link NullPointerException} - Null pointer errors (HTTP 500)</li>
 * <li>{@link ApiException} - Custom business logic exceptions (custom HTTP status)</li>
 * </ul>
 *
 * @author Pasindu OG
 * @version 2.0.0
 * @since 1.2.0
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
    public ProblemDetail handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String errorMessage = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s.",
                ex.getValue(), ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        log.warn("Type mismatch error: {}", errorMessage);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorMessage);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles malformed JSON errors.
     * <p>
     * Triggered when the request body is invalid JSON (e.g., missing commas, brackets, or wrong types).
     * </p>
     *
     * @param ex The exception thrown when JSON parsing fails.
     * @return A {@link ProblemDetail} with HTTP 400 status.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON request: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed JSON request. Please check your request body format.");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles missing required parameters.
     * <p>
     * Triggered when a required @RequestParam is missing from the request URL.
     * </p>
     *
     * @param ex The exception containing the missing parameter name.
     * @return A {@link ProblemDetail} with HTTP 400 status.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        String message = String.format("Required request parameter '%s' (type: %s) is missing.",
                ex.getParameterName(), ex.getParameterType());
        log.warn("Missing parameter: {}", message);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles 404 Not Found errors for static resources and missing endpoints (Spring Boot 3.2+).
     * <p>
     * This eliminates the need for configuring `spring.mvc.throw-exception-if-no-handler-found`.
     * It catches the exception thrown when no controller or static resource is found.
     * </p>
     *
     * @param ex The exception containing the requested resource path.
     * @return A {@link ProblemDetail} with HTTP 404 status.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFoundException(NoResourceFoundException ex) {
        String message = String.format("The requested resource '/%s' was not found.", ex.getResourcePath());
        log.warn("404 Not Found: {}", message);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, message);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles unsupported HTTP methods (405).
     * <p>
     * Triggered when a user sends a request with a method (e.g., POST) that is not supported
     * by the endpoint (e.g., it only expects GET).
     * </p>
     *
     * @param ex The exception containing the supported methods.
     * @return A {@link ProblemDetail} with HTTP 405 status.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        String message = String.format("Method '%s' is not supported for this endpoint. Supported methods are: %s",
                ex.getMethod(), ex.getSupportedHttpMethods());
        log.warn("Method not allowed: {}", message);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED, message);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles unsupported media types (415).
     * <p>
     * Triggered when the client sends a Content-Type (e.g., application/xml) that the server cannot process.
     * </p>
     *
     * @param ex The exception containing the supported media types.
     * @return A {@link ProblemDetail} with HTTP 415 status.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        String message = String.format("Content type '%s' is not supported. Supported content types: %s",
                ex.getContentType(), ex.getSupportedMediaTypes());
        log.warn("Unsupported media type: {}", message);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message);
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