package io.github.pasinduog.exception;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
import java.util.UUID;

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
 * <p>
 * <b>New in v2.0.0:</b> All error responses now include a traceId from SLF4J MDC for
 * distributed tracing and log correlation. If no traceId exists in MDC, a random UUID
 * is generated automatically.
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
 * <h2>Response Format:</h2>
 * <p>
 * All responses follow RFC 7807 ProblemDetail structure with additional properties:
 * </p>
 * <ul>
 * <li><b>type</b> - A URI reference identifying the problem type</li>
 * <li><b>title</b> - A short, human-readable summary of the problem type</li>
 * <li><b>status</b> - The HTTP status code</li>
 * <li><b>detail</b> - A human-readable explanation of the error</li>
 * <li><b>instance</b> - A URI reference identifying the specific occurrence</li>
 * <li><b>traceId</b> - UUID for request tracing and log correlation (custom property)</li>
 * <li><b>timestamp</b> - ISO-8601 timestamp when the error occurred (custom property)</li>
 * <li><b>errors</b> - Field-level validation errors map (for validation failures only)</li>
 * </ul>
 *
 * @author Pasindu OG
 * @version 2.0.1
 * @since 1.2.0
 * @see ProblemDetail
 * @see ApiException
 * @see org.slf4j.MDC
 */
@ConditionalOnProperty(
        prefix = "api-response",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RestControllerAdvice
@Slf4j
@SuppressWarnings("unused")
public class GlobalExceptionHandler {

    /**
     * Default constructor for GlobalExceptionHandler.
     * <p>
     * This constructor is automatically invoked by Spring's dependency injection
     * container when registering this exception handler as a bean.
     * </p>
     */
    public GlobalExceptionHandler() {
        // Default constructor for Spring bean instantiation
    }

    /**
     * Gets or generates a trace ID for error tracking.
     * <p>
     * Priority: 1. MDC context, 2. Generate new UUID.
     * This ensures consistent trace ID between logs and error responses.
     * </p>
     *
     * @return The trace ID from MDC or a newly generated UUID.
     */
    private String getOrGenerateTraceId() {
        String traceId = MDC.get("traceId");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
            MDC.put("traceId", traceId);
        }
        return traceId;
    }

    /**
     * Handles all unexpected exceptions (catch-all handler).
     * <p>
     * This method acts as a safety net for any error not explicitly handled by other methods.
     * It returns a generic 500 Internal Server Error to avoid leaking sensitive stack traces
     * to the client, while logging the full error details for debugging.
     * </p>
     * <p>
     * The response includes a traceId from SLF4J MDC (or a generated UUID) and a timestamp
     * to help with error tracking and log correlation.
     * </p>
     *
     * @param ex The exception that was thrown.
     * @return A {@link ProblemDetail} with HTTP 500 status, traceId, timestamp, and a generic error message.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAllExceptions(Exception ex) {
        String traceId = getOrGenerateTraceId();
        StackTraceElement rootCause = ex.getStackTrace().length > 0 ? ex.getStackTrace()[0] : null;
        String className = (rootCause != null) ? rootCause.getClassName() : "Unknown Class";
        int lineNumber = (rootCause != null) ? rootCause.getLineNumber() : -1;

        log.error("[TraceID: {}] Error in {}:{} - Message: {}",
                traceId, className, lineNumber, ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error. Please contact technical support");
        problemDetail.setProperty("traceId", traceId);
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
     * <p>
     * The response includes a traceId from SLF4J MDC (or a generated UUID), timestamp, and
     * a detailed map of field validation errors.
     * </p>
     *
     * @param ex The validation exception containing the list of field errors.
     * @return A {@link ProblemDetail} with HTTP 400 status, traceId, timestamp, and a map of validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        String traceId = getOrGenerateTraceId();
        Map<String, String> errorMessage = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            assert fieldError.getDefaultMessage() != null;
            // If multiple errors exist for the same field, merge them
            errorMessage.merge(fieldError.getField(), fieldError.getDefaultMessage(),
                    (msg1, msg2) -> msg1 + "; " + msg2);
        }
        log.warn("[TraceID: {}] Validation error: {}", traceId, errorMessage);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation Failed");
        problemDetail.setProperty("errors", errorMessage);
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles type mismatch errors for method arguments.
     * <p>
     * This occurs when a request parameter cannot be converted to the expected type,
     * such as passing a string where an integer is required.
     * </p>
     * <p>
     * The response includes a consistent traceId (from MDC or generated) and timestamp
     * to help with debugging and log correlation.
     * </p>
     *
     * @param ex The type mismatch exception.
     * @return A {@link ProblemDetail} with HTTP 400 status, traceId, timestamp, and a descriptive error message.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String traceId = getOrGenerateTraceId();
        String errorMessage = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s.",
                ex.getValue(), ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        log.warn("[TraceID: {}] Type mismatch error: {}", traceId, errorMessage);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorMessage);
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles malformed JSON errors.
     * <p>
     * Triggered when the request body is invalid JSON (e.g., missing commas, brackets, or wrong types).
     * </p>
     * <p>
     * The response includes a consistent traceId (from MDC or generated) and timestamp
     * for debugging and log correlation.
     * </p>
     *
     * @param ex The exception thrown when JSON parsing fails.
     * @return A {@link ProblemDetail} with HTTP 400 status, traceId, and timestamp.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String traceId = getOrGenerateTraceId();
        log.warn("[TraceID: {}] Malformed JSON request: {}", traceId, ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed JSON request. Please check your request body format.");
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles missing required parameters.
     * <p>
     * Triggered when a required @RequestParam is missing from the request URL.
     * </p>
     * <p>
     * The response includes a traceId from SLF4J MDC (or a generated UUID) and timestamp
     * for debugging and log correlation.
     * </p>
     *
     * @param ex The exception containing the missing parameter name.
     * @return A {@link ProblemDetail} with HTTP 400 status, traceId, and timestamp.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        String traceId = getOrGenerateTraceId();
        String message = String.format("Required request parameter '%s' (type: %s) is missing.",
                ex.getParameterName(), ex.getParameterType());
        log.warn("[TraceID: {}] Missing parameter: {}", traceId, message);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles 404 Not Found errors for static resources and missing endpoints (Spring Boot 3.2+).
     * <p>
     * This eliminates the need for configuring {@code spring.mvc.throw-exception-if-no-handler-found}.
     * It catches the exception thrown when no controller or static resource is found.
     * </p>
     * <p>
     * The response includes a consistent traceId (from MDC or generated) and timestamp
     * for debugging and log correlation.
     * </p>
     *
     * @param ex The exception containing the requested resource path.
     * @return A {@link ProblemDetail} with HTTP 404 status, traceId, and timestamp.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFoundException(NoResourceFoundException ex) {
        String traceId = getOrGenerateTraceId();
        String message = String.format("The requested resource '/%s' was not found.", ex.getResourcePath());
        log.warn("[TraceID: {}] 404 Not Found: {}", traceId, message);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, message);
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles unsupported HTTP methods (405).
     * <p>
     * Triggered when a user sends a request with a method (e.g., POST) that is not supported
     * by the endpoint (e.g., it only expects GET).
     * </p>
     * <p>
     * The response includes a consistent traceId (from MDC or generated) and timestamp
     * for debugging and log correlation.
     * </p>
     *
     * @param ex The exception containing the supported methods.
     * @return A {@link ProblemDetail} with HTTP 405 status, traceId, and timestamp.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        String traceId = getOrGenerateTraceId();
        String message = String.format("Method '%s' is not supported for this endpoint. Supported methods are: %s",
                ex.getMethod(), ex.getSupportedHttpMethods());
        log.warn("[TraceID: {}] Method not allowed: {}", traceId, message);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED, message);
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles unsupported media types (415).
     * <p>
     * Triggered when the client sends a Content-Type (e.g., application/xml) that the server cannot process.
     * </p>
     * <p>
     * The response includes a traceId from SLF4J MDC (or a generated UUID) and timestamp
     * for debugging and log correlation.
     * </p>
     *
     * @param ex The exception containing the supported media types.
     * @return A {@link ProblemDetail} with HTTP 415 status, traceId, and timestamp.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        String traceId = getOrGenerateTraceId();
        String message = String.format("Content type '%s' is not supported. Supported content types: %s",
                ex.getContentType(), ex.getSupportedMediaTypes());
        log.warn("[TraceID: {}] Unsupported media type: {}", traceId, message);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message);
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles NullPointerExceptions explicitly.
     * <p>
     * While strictly not necessary (as the catch-all handler covers this), having a specific
     * handler allows for distinct logging or custom messaging for NPEs if required in the future.
     * </p>
     * <p>
     * The response includes a consistent traceId (from MDC or generated) and timestamp
     * for debugging and log correlation.
     * </p>
     *
     * @param ex The NullPointerException that was thrown.
     * @return A {@link ProblemDetail} with HTTP 500 status, traceId, and timestamp.
     */
    @ExceptionHandler(NullPointerException.class)
    public ProblemDetail handleNullPointerExceptions(NullPointerException ex) {
        String traceId = getOrGenerateTraceId();
        log.error("[TraceID: {}] Null pointer exception occurred: ", traceId, ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "A null pointer exception occurred.");
        problemDetail.setProperty("traceId", traceId);
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
     * <p>
     * The response includes a consistent traceId (from MDC or generated) and timestamp
     * for debugging and log correlation across distributed systems.
     * </p>
     *
     * @param ex The custom API exception instance.
     * @return A {@link ProblemDetail} with the status and message defined in the exception, plus traceId and timestamp.
     */
    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException ex) {
        String traceId = getOrGenerateTraceId();
        log.warn("[TraceID: {}] Business logic exception: {} | Status: {}", traceId, ex.getMessage(), ex.getStatus());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }
}