package io.github.pasinduog.config;

import io.github.pasinduog.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Autoconfiguration class for the API Response Library.
 * <p>
 * This configuration is automatically loaded by Spring Boot's autoconfiguration mechanism
 * when the library is present on the classpath. It registers essential beans required for
 * the library to function properly, including the global exception handler.
 * </p>
 * <p>
 * <b>No manual configuration is required.</b> Simply adding the library dependency will
 * enable all features automatically.
 * </p>
 * <h2>What Gets Auto-Configured:</h2>
 * <ul>
 *   <li>{@link GlobalExceptionHandler} - Automatic exception handling with RFC 7807 ProblemDetail format</li>
 * </ul>
 * <h2>Disabling Auto-Configuration:</h2>
 * <p>
 * If you need to disable this autoconfiguration, you can exclude it in your main application class:
 * </p>
 * <pre>
 * {@code
 * @SpringBootApplication(exclude = ApiResponseAutoConfiguration.class)
 * public class Application {
 *     public static void main(String[] args) {
 *         SpringApplication.run(Application.class, args);
 *     }
 * }
 * }
 * </pre>
 *
 * @author Pasindu OG
 * @version 2.0.0
 * @since 1.3.0
 * @see GlobalExceptionHandler
 */
@Configuration
@SuppressWarnings("unused")
public class ApiResponseAutoConfiguration {

    /**
     * Default constructor for ApiResponseAutoConfiguration.
     * <p>
     * This constructor is automatically invoked by Spring's dependency injection
     * container during application startup when autoconfiguration is enabled.
     * </p>
     */
    public ApiResponseAutoConfiguration() {
        // Default constructor for Spring autoconfiguration
    }

    /**
     * Registers the {@link GlobalExceptionHandler} as a Spring bean.
     * <p>
     * The handler provides centralized exception management using Spring's
     * {@link org.springframework.web.bind.annotation.RestControllerAdvice} mechanism,
     * automatically converting exceptions to RFC 7807 ProblemDetail responses.
     * </p>
     *
     * @return A new instance of {@link GlobalExceptionHandler}.
     */
    @Bean
    public GlobalExceptionHandler apiResponseAdvisor() {
        return new GlobalExceptionHandler();
    }
}
