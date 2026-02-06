package io.github.pasinduog.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * Standard API Response wrapper for Spring Boot Applications.
 * <p>
 * This class provides a consistent structure for all API responses, ensuring that
 * clients always receive a predictable format including a status code, message,
 * content payload, and metadata like timestamps and trace IDs for debugging.
 * </p>
 * <p>
 * <b>New in v2.0.0:</b> Automatic traceId generation from SLF4J MDC for distributed
 * tracing support. If a traceId exists in the MDC context (e.g., from {@link io.github.pasinduog.filter.TraceIdFilter}),
 * it will be used automatically. Otherwise, a random UUID is generated.
 * </p>
 *
 * <h2>Key Features:</h2>
 * <ul>
 *   <li>Consistent response structure across all API endpoints</li>
 *   <li>Automatic traceId generation for request tracking</li>
 *   <li>ISO-8601 UTC timestamps for all responses</li>
 *   <li>Null-safe with Jackson's @JsonInclude for clean JSON output</li>
 *   <li>Fluent builder pattern via Lombok @Builder</li>
 *   <li>Static factory methods for common HTTP status codes</li>
 * </ul>
 *
 * <h2>Usage Examples:</h2>
 * <pre>
 * {@code
 * // Simple success response with content
 * return ApiResponse.success("User fetched successfully", userDto);
 *
 * // Created response (201)
 * return ApiResponse.created("User created successfully", savedUser);
 *
 * // Success without content
 * return ApiResponse.success("User deleted successfully");
 *
 * // Custom status code
 * return ApiResponse.status("Custom message", content, HttpStatus.ACCEPTED);
 * }
 * </pre>
 *
 * @param <T> The type of the content object included in the response body.
 * @author Pasindu OG
 * @version 2.0.0
 * @since 1.0.0
 * @see org.springframework.http.ResponseEntity
 * @see org.slf4j.MDC
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
     * <p>
     * Useful for distributed tracing and log correlation in microservices.
     * Automatically retrieved from SLF4J's MDC (Mapped Diagnostic Context) if available,
     * or defaults to a randomly generated UUID if no traceId exists in MDC.
     * </p>
     * <p>
     * When using {@link io.github.pasinduog.filter.TraceIdFilter}, this will automatically
     * contain the filter-generated traceId for seamless request tracking.
     * </p>
     *
     * @see org.slf4j.MDC
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Builder.Default
    private final UUID traceId = MDC.get("traceId") != null ? UUID.fromString(MDC.get("traceId")) : UUID.randomUUID();

    /**
     * A descriptive message about the API result (e.g., "Operation successful" or error details).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String message;

    /**
     * The actual payload of the response.
     * Can be a DTO, a List, or null if no content is returned.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T content;

    /**
     * The UTC timestamp when the response was generated.
     * <p>
     * Defaults to the current system time in UTC using {@link Instant#now()}.
     * The timestamp is in ISO-8601 format when serialized to JSON.
     * </p>
     * <p>
     * Example JSON output: {@code "timestamp": "2026-02-06T10:30:45.123Z"}
     * </p>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Builder.Default
    private final Instant timestamp = Instant.now();

    /**
     * Constructs an ApiResponse with all parameters.
     * <p>
     * This constructor is used internally by Lombok's {@code @Builder} annotation.
     * It is recommended to use static factory methods like {@link #success(String, Object)},
     * {@link #created(String, Object)}, or the builder pattern instead of calling this directly.
     * </p>
     *
     * @param status The HTTP status code.
     * @param traceId The unique trace identifier for this request.
     * @param message A descriptive message about the result.
     * @param content The response payload content.
     * @param timestamp The timestamp when the response was created.
     */
    public ApiResponse(Integer status, UUID traceId, String message, T content, Instant timestamp) {
        this.status = status;
        this.traceId = traceId;
        this.message = message;
        this.content = content;
        this.timestamp = timestamp;
    }

    /**
     * Creates a response with HTTP 201 Created status.
     * <p>
     * This is typically used for POST requests where a new resource has been successfully created.
     * </p>
     *
     * <h3>Example Usage:</h3>
     * <pre>
     * {@code
     * @PostMapping("/users")
     * public ResponseEntity<ApiResponse<UserDto>> createUser(@RequestBody UserDto userDto) {
     *     UserDto savedUser = userService.save(userDto);
     *     return ApiResponse.created("User created successfully", savedUser);
     * }
     * }
     * </pre>
     *
     * @param message The success message to be displayed to the client.
     * @param content    The created content object (e.g., the saved User DTO).
     * @param <T>     The type of the content.
     * @return A {@link ResponseEntity} containing the {@link ApiResponse} with HTTP 201 status.
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T content) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<T>builder()
                        .status(HttpStatus.CREATED.value())
                        .message(message)
                        .content(content)
                        .build());
    }

    /**
     * Creates a success response with HTTP 200 OK status without a content payload.
     * <p>
     * Useful for operations like DELETE or PUT where the action is successful but there is no
     * specific content to return.
     * </p>
     *
     * <h3>Example Usage:</h3>
     * <pre>
     * {@code
     * @DeleteMapping("/users/{id}")
     * public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
     *     userService.delete(id);
     *     return ApiResponse.success("User deleted successfully");
     * }
     * }
     * </pre>
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
     * Creates a success response with HTTP 200 OK status and a content payload.
     * <p>
     * This is the most common method used for GET requests to return requested content.
     * </p>
     *
     * <h3>Example Usage:</h3>
     * <pre>
     * {@code
     * @GetMapping("/users/{id}")
     * public ResponseEntity<ApiResponse<UserDto>> getUser(@PathVariable Long id) {
     *     UserDto user = userService.findById(id);
     *     return ApiResponse.success("User fetched successfully", user);
     * }
     *
     * @GetMapping("/users")
     * public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
     *     List<UserDto> users = userService.findAll();
     *     return ApiResponse.success("Users fetched successfully", users);
     * }
     * }
     * </pre>
     *
     * @param message The success message.
     * @param content    The content object to return (e.g., a UserDto or List&lt;UserDto&gt;).
     * @param <T>     The type of the content.
     * @return A {@link ResponseEntity} containing the {@link ApiResponse} with HTTP 200 status.
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T content) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<T>builder()
                        .status(HttpStatus.OK.value())
                        .message(message)
                        .content(content)
                        .build());
    }

    /**
     * Creates a response with a custom HTTP status and no content payload.
     * <p>
     * Useful for error handling or specific status codes not covered by standard success methods.
     * </p>
     *
     * <h3>Example Usage:</h3>
     * <pre>
     * {@code
     * @PostMapping("/process")
     * public ResponseEntity<ApiResponse<Void>> process() {
     *     // Queue the processing job
     *     jobService.queueJob();
     *     return ApiResponse.status("Processing queued", HttpStatus.ACCEPTED);
     * }
     * }
     * </pre>
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
     * Creates a response with a custom HTTP status and a content payload.
     * <p>
     * Provides maximum flexibility for custom response scenarios with any HTTP status code.
     * </p>
     *
     * <h3>Example Usage:</h3>
     * <pre>
     * {@code
     * @PutMapping("/users/{id}")
     * public ResponseEntity<ApiResponse<UserDto>> updateUser(@PathVariable Long id, @RequestBody UserDto dto) {
     *     UserDto updated = userService.update(id, dto);
     *     return ApiResponse.status("User updated and queued for sync", updated, HttpStatus.ACCEPTED);
     * }
     * }
     * </pre>
     *
     * @param message The message to include in the response.
     * @param content    The content object to return.
     * @param status  The specific {@link HttpStatus} to return.
     * @param <T>     The type of the content.
     * @return A {@link ResponseEntity} containing the {@link ApiResponse} with the specified status.
     */
    public static <T> ResponseEntity<ApiResponse<T>> status(String message, T content, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(ApiResponse.<T>builder()
                        .status(status.value())
                        .message(message)
                        .content(content)
                        .build());
    }
}