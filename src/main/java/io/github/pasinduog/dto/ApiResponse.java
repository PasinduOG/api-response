package io.github.pasinduog.dto;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

/**
 * Standard API Response wrapper for Spring Boot Applications.
 * This class provides a consistent structure for API responses including a message, data payload, and timestamp.
 *
 * @param <T> The type of the data object included in the response.
 */
@Getter
@SuppressWarnings("unused")
public class ApiResponse<T> {
    private final String message;
    private final T data;
    private final LocalDateTime timestamp;

    /**
     * Private constructor to enforce the usage of static factory methods.
     * Initializes the timestamp to the current time.
     *
     * @param message The message to be included in the response.
     * @param data    The data object to be included in the response.
     */
    private ApiResponse(String message, T data) {
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Creates a response with HTTP 201 Created status.
     * Useful for resource creation endpoints.
     *
     * @param message The success message.
     * @param data    The created data object.
     * @param <T>     The type of the data.
     * @return A ResponseEntity containing the ApiResponse with HTTP 201 status.
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(message, data));
    }

    /**
     * Creates a success response with HTTP 200 OK status.
     * This method is used when there is no data payload to return.
     *
     * @param message The success message.
     * @return A ResponseEntity containing the ApiResponse with HTTP 200 status.
     */
    public static ResponseEntity<ApiResponse<Void>> success(String message) {
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(message, null));
    }

    /**
     * Creates a success response with HTTP 200 OK status and a data payload.
     *
     * @param message The success message.
     * @param data    The data object to return.
     * @param <T>     The type of the data.
     * @return A ResponseEntity containing the ApiResponse with HTTP 200 status.
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(message, data));
    }

    /**
     * Creates a response with a custom HTTP status and no data payload.
     *
     * @param message The message to include in the response.
     * @param status  The specific HttpStatus to return.
     * @return A ResponseEntity containing the ApiResponse with the specified status.
     */
    public static ResponseEntity<ApiResponse<Void>> status(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(new ApiResponse<>(message, null));
    }

    /**
     * Creates a response with a custom HTTP status and a data payload.
     *
     * @param message The message to include in the response.
     * @param data    The data object to return.
     * @param status  The specific HttpStatus to return.
     * @param <T>     The type of the data.
     * @return A ResponseEntity containing the ApiResponse with the specified status.
     */
    public static <T> ResponseEntity<ApiResponse<T>> status(String message, T data, HttpStatus status) {
        return ResponseEntity.status(status).body(new ApiResponse<>(message, data));
    }
}