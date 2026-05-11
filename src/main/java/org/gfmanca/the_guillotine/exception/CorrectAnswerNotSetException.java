package org.gfmanca.the_guillotine.exception;

/**
 * Exception thrown when an attempt is made to process a quiz without setting a correct answer.
 *
 * This situation typically arises during the evaluation or submission phases of a quiz
 * workflow, and indicates that the quiz is in an incomplete or invalid state.
 *
 * The exception message includes the quiz ID, providing context for identifying
 * the specific quiz associated with this issue.
 */
public class CorrectAnswerNotSetException extends RuntimeException {

    public CorrectAnswerNotSetException(Long quizId) {
        super(String.format("La risposta corretta per il quiz %d non è stata impostata.", quizId));
    }
}
