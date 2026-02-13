package io.github.pasinduog.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

/**
 * Standard API Response wrapper for Spring Boot applications.
 * <p>
 * This class provides a consistent structure for API responses across your application,
 * including status codes, messages, content payload, and timestamps. It supports both
 * successful and error responses with optional content.
 * </p>
 *
 * @param <T> the type of the response content
 * @author Pasindu OG
 * @version 2.0.0
 * @since 1.0.0
 */
@SuppressWarnings({"unused"})
public class ApiResponse<T> {

    /**
     * The HTTP status code of the response.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Integer status;

    /**
     * A descriptive message about the response.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String message;

    /**
     * The response content/payload.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T content;

    /**
     * The timestamp when the response was created.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Instant timestamp;

    /**
     * Private constructor that builds an ApiResponse from a builder.
     *
     * @param builder the ApiResponseBuilder containing the response data
     */
    private ApiResponse(ApiResponseBuilder<T> builder) {
        this.status = builder.status;
        this.message = builder.message;
        this.content = builder.content;
        this.timestamp = Instant.now();
    }

    /**
     * Gets the HTTP status code.
     *
     * @return the status code
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * Gets the response message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the response content.
     *
     * @return the content
     */
    public T getContent() {
        return content;
    }

    /**
     * Gets the response timestamp.
     *
     * @return the timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Creates a CREATED (201) response with a message and content.
     *
     * @param <T> the type of the response content
     * @param message the response message
     * @param content the response content
     * @return a ResponseEntity with CREATED status
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T content) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseBuilder<T>()
                        .status(HttpStatus.CREATED.value())
                        .message(message)
                        .content(content)
                        .build());
    }

    /**
     * Creates a SUCCESS (200) response with only a message.
     *
     * @param message the response message
     * @return a ResponseEntity with OK status
     */
    public static ResponseEntity<ApiResponse<Void>> success(String message) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseBuilder<Void>()
                        .status(HttpStatus.OK.value())
                        .message(message)
                        .build());
    }

    /**
     * Creates a SUCCESS (200) response with a message and content.
     *
     * @param <T> the type of the response content
     * @param message the response message
     * @param content the response content
     * @return a ResponseEntity with OK status
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T content) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseBuilder<T>()
                        .status(HttpStatus.OK.value())
                        .message(message)
                        .content(content)
                        .build());
    }

    /**
     * Creates a response with a custom HTTP status and message only.
     *
     * @param message the response message
     * @param status the HTTP status
     * @return a ResponseEntity with the specified status
     */
    public static ResponseEntity<ApiResponse<Void>> status(String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(new ApiResponseBuilder<Void>()
                        .status(status.value())
                        .message(message)
                        .build());
    }

    /**
     * Creates a response with a custom HTTP status, message, and content.
     *
     * @param <T> the type of the response content
     * @param message the response message
     * @param content the response content
     * @param status the HTTP status
     * @return a ResponseEntity with the specified status
     */
    public static <T> ResponseEntity<ApiResponse<T>> status(String message, T content, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(new ApiResponseBuilder<T>()
                        .status(status.value())
                        .message(message)
                        .content(content)
                        .build());
    }

    /**
     * Builder class for constructing ApiResponse instances.
     *
     * @param <T> the type of the response content
     */
    public static class ApiResponseBuilder<T> {
        private Integer status;
        private String message;
        private T content;

        /**
         * Sets the HTTP status code.
         *
         * @param status the status code
         * @return this builder instance
         */
        private ApiResponseBuilder<T> status(Integer status) {
            this.status = status;
            return this;
        }

        /**
         * Sets the response message.
         *
         * @param message the message
         * @return this builder instance
         */
        public ApiResponseBuilder<T> message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Sets the response content.
         *
         * @param content the content
         * @return this builder instance
         */
        public ApiResponseBuilder<T> content(T content) {
            this.content = content;
            return this;
        }

        /**
         * Builds the ApiResponse instance.
         *
         * @return a new ApiResponse instance
         */
        public ApiResponse<T> build() {
            return new ApiResponse<>(this);
        }
    }
}