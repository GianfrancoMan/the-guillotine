package org.gfmanca.the_guillotine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * A global exception handler that intercepts and processes application exceptions
 * to provide uniform error responses. Each exception type is individually handled
 * by the corresponding method, which generates an HTTP response with an appropriate
 * status code and structured error message.
 *
 * This class is annotated with {@code @RestControllerAdvice}, making it applicable
 * across all controllers in the application. It helps maintain consistency in error
 * responses and eliminates repetitive exception-handling logic in individual controllers.
 *
 * Exception handling methods:
 * - {@code handleQuizNotFound(QuizNotFoundException ex)}: Handles cases where a quiz
 *   with the specified ID is not found, returning an HTTP 404.
 * - {@code handleUserNotFound(UserNotFoundException ex)}: Handles cases where a user
 *   with the specified ID is not found, returning an HTTP 404.
 * - {@code handleQuizClosed(QuizClosedException ex)}: Handles cases where a quiz is closed
 *   and submissions are not allowed, returning an HTTP 403.
 * - {@code handleDuplicateSubmission(DuplicateSubmissionException ex)}: Handles cases where
 *   a user attempts to submit a duplicate response for a quiz, returning an HTTP 409.
 * - {@code handleValidationErrors(MethodArgumentNotValidException ex)}: Handles validation
 *   errors resulting from invalid input, returning an HTTP 400 along with details of the
 *   specific validation failure.
 *
 * Each exception-handling method delegates the response-building logic to
 * {@code buildErrorResponse(HttpStatus status, String message)}, which generates a
 * standardized response body containing the following fields:
 * - {@code timestamp}: The current time at which the error occurred.
 * - {@code status}: The HTTP status code of the response.
 * - {@code error}: The reason phrase associated with the HTTP status code.
 * - {@code message}: A descriptive error message explaining the issue.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(QuizNotFoundException.class)
    public ResponseEntity<?> handleQuizNotFound(QuizNotFoundException ex) {

        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(UserNotFoundException ex) {

        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(QuizClosedException.class)
    public ResponseEntity<?> handleQuizClosed(QuizClosedException ex) {

        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(DuplicateSubmissionException.class)
    public ResponseEntity<?> handleDuplicateSubmission(DuplicateSubmissionException ex) {

        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Validation error");

        return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse( HttpStatus status, String message) {

        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        );

        return ResponseEntity.status(status).body(body);
    }
}