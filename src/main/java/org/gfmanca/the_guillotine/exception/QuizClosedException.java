package org.gfmanca.the_guillotine.exception;

public class QuizClosedException extends RuntimeException {

    public QuizClosedException(long quizId) {
        super(String.format("Errore! non puoi inviare risposte, questo quiz con ID %d non è aperto.", quizId));
    }
}
