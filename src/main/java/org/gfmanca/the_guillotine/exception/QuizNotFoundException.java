package org.gfmanca.the_guillotine.exception;

public class QuizNotFoundException extends RuntimeException {
    public QuizNotFoundException(Long quizId) {
        super(String.format("Errore! quiz con ID %d non trovato.", quizId));
    }
}
