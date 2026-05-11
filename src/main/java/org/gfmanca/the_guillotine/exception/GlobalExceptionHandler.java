package org.gfmanca.the_guillotine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Centralizes exception handling for REST controllers.
 *
 * Converts application and validation exceptions into consistent HTTP error
 * responses with a standard response body.
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles requests for quizzes that do not exist.
     *
     * @param ex the thrown quiz-not-found exception
     * @return a 404 Not Found error response
     */
    @ExceptionHandler(QuizNotFoundException.class)
    public ResponseEntity<?> handleQuizNotFound(QuizNotFoundException ex) {

        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handles requests for users that do not exist.
     *
     * @param ex the thrown user-not-found exception
     * @return a 404 Not Found error response
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(UserNotFoundException ex) {

        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handles submissions attempted after a quiz has been closed.
     *
     * @param ex the thrown quiz-closed exception
     * @return a 403 Forbidden error response
     */
    @ExceptionHandler(QuizClosedException.class)
    public ResponseEntity<?> handleQuizClosed(QuizClosedException ex) {

        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    /**
     * Handles duplicate quiz submissions by the same user.
     *
     * @param ex the thrown duplicate-submission exception
     * @return a 409 Conflict error response
     */
    @ExceptionHandler(DuplicateSubmissionException.class)
    public ResponseEntity<?> handleDuplicateSubmission(DuplicateSubmissionException ex) {

        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Handles validation failures for invalid request payloads.
     *
     * @param ex the validation exception containing field errors
     * @return a 400 Bad Request error response with the first validation error
     */
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

    /**
     * Handles cases where no winner can be found for a quiz.
     *
     * @param ex the thrown no-winner-found exception
     * @return a 404 Not Found error response
     */
    @ExceptionHandler(NoWinnerFoundException.class)
    public ResponseEntity<?> handleNoWinnerFound(NoWinnerFoundException ex) {

        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handles cases where an attempt is made to process a quiz without setting a correct answer.
     *
     * This method listens for the {@link CorrectAnswerNotSetException} and generates a standardized
     * 400 Bad Request error response indicating that the quiz is in an invalid or incomplete state.
     *
     * @param ex the exception thrown when the correct answer is not set for a quiz
     * @return a response entity containing the error response with HTTP status 400 and a detailed message
     */
    @ExceptionHandler(CorrectAnswerNotSetException.class)
    public ResponseEntity<?> handleCorrectAnswerNotSet(CorrectAnswerNotSetException ex) {

        return buildErrorResponse(HttpStatus.BAD_REQUEST,  ex.getMessage());
    }

    /**
     * Builds a consistent error response body.
     *
     * @param status the HTTP status to return
     * @param message the error message to include in the response
     * @return a response entity containing the standardized error body
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {

        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        );

        return ResponseEntity.status(status).body(body);
    }
}