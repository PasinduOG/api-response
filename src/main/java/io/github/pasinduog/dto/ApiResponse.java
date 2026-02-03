package io.github.pasinduog.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * Standard API Response wrapper for Spring Boot Applications.
 * <p>
 * This class provides a consistent structure for all API responses, ensuring that
 * clients always receive a predictable format including a status code, message,
 * data payload, and metadata like timestamps and trace IDs for debugging.
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>
 * {@code
 * return ApiResponse.success("User fetched successfully", userDto);
 * }
 * </pre>
 *
 * @param <T> The type of the data object included in the response body.
 * @author Pasindu OG
 * @version 2.0.0
 * @since 1.0.0
 */
@Getter
@Builder
@SuppressWarnings("unused")
public class ApiResponse<T> {

    /**
     * The HTTP status code of the response (e.g., 200, 201, 400).
     * This mirrors the HTTP status but is included in the body for easier client-side parsing.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Integer status;

    /**
     * A unique identifier for the request/response cycle.
     * Useful for distributed tracing and log correlation in microservices.
     * Defaults to a random UUID if not provided.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Builder.Default
    private final UUID traceId = UUID.randomUUID();

    /**
     * A descriptive message about the API result (e.g., "Operation successful" or error details).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String message;

    /**
     * The actual payload of the response.
     * Can be a DTO, a List, or null if no data is returned.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;

    /**
     * The UTC timestamp when the response was generated.
     * Defaults to the current system time in UTC.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Builder.Default
    private final Instant timestamp = Instant.now();

    /**
     * Creates a response with HTTP 201 Created status.
     * <p>
     * This is typically used for POST requests where a new resource has been successfully created.
     * </p>
     *
     * @param message The success message to be displayed to the client.
     * @param data    The created data object (e.g., the saved User DTO).
     * @param <T>     The type of the data.
     * @return A {@link ResponseEntity} containing the {@link ApiResponse} with HTTP 201 status.
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<T>builder()
                        .status(HttpStatus.CREATED.value())
                        .message(message)
                        .data(data)
                        .build());
    }

    /**
     * Creates a success response with HTTP 200 OK status without a data payload.
     * <p>
     * Useful for operations like DELETE or PUT where the action is successful but there is no
     * specific content to return.
     * </p>
     *
     * @param message The success message.
     * @return A {@link ResponseEntity} containing the {@link ApiResponse} with HTTP 200 status.
     */
    public static ResponseEntity<ApiResponse<Void>> success(String message) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message(message)
                        .build());
    }

    /**
     * Creates a success response with HTTP 200 OK status and a data payload.
     * <p>
     * This is the most common method used for GET requests to return requested data.
     * </p>
     *
     * @param message The success message.
     * @param data    The data object to return (e.g., a UserDto or List&lt;UserDto&gt;).
     * @param <T>     The type of the data.
     * @return A {@link ResponseEntity} containing the {@link ApiResponse} with HTTP 200 status.
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<T>builder()
                        .status(HttpStatus.OK.value())
                        .message(message)
                        .data(data)
                        .build());
    }

    /**
     * Creates a response with a custom HTTP status and no data payload.
     * <p>
     * Useful for error handling or specific status codes not covered by standard success methods.
     * </p>
     *
     * @param message The message to include in the response.
     * @param status  The specific {@link HttpStatus} to return.
     * @return A {@link ResponseEntity} containing the {@link ApiResponse} with the specified status.
     */
    public static ResponseEntity<ApiResponse<Void>> status(String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(ApiResponse.<Void>builder()
                        .status(status.value())
                        .message(message)
                        .build());
    }

    /**
     * Creates a response with a custom HTTP status and a data payload.
     *
     * @param message The message to include in the response.
     * @param data    The data object to return.
     * @param status  The specific {@link HttpStatus} to return.
     * @param <T>     The type of the data.
     * @return A {@link ResponseEntity} containing the {@link ApiResponse} with the specified status.
     */
    public static <T> ResponseEntity<ApiResponse<T>> status(String message, T data, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(ApiResponse.<T>builder()
                        .status(status.value())
                        .message(message)
                        .data(data)
                        .build());
    }
}