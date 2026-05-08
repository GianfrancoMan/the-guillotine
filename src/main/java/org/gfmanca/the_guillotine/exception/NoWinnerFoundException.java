package org.gfmanca.the_guillotine.exception;

/**
 * Exception thrown when no winner is found for a specific quiz based on a given answer.
 * This might occur when the quiz has been evaluated, but no participant meets
 * the win criteria for the provided answer.
 *
 * The exception message includes the quiz ID and the answer that resulted
 * in no winner being identified, providing clarity about the context in which
 * the exception was raised.
 *
 * This is a runtime exception, typically used in scenarios where such conditions
 * are unexpected and indicative of a missing or incorrect state in quiz evaluation.
 */
public class NoWinnerFoundException extends RuntimeException {

    public NoWinnerFoundException(Long quizId, String answer) {

        super(String.format("Nessun vincitore trovato per il quiz %d con risposta '%s'.", quizId,  answer));
    }
}
