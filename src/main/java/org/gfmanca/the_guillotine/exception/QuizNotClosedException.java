package org.gfmanca.the_guillotine.exception;

public class QuizNotClosedException extends RuntimeException {
    
    public QuizNotClosedException(long quizId) {
        super(String.format("Errore! non puoi impostare la risposta correttail quiz con ID:%d non è chiuso.", quizId));
    }
    
}
