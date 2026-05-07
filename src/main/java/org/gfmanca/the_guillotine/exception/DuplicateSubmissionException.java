package org.gfmanca.the_guillotine.exception;

public class DuplicateSubmissionException extends RuntimeException {

    public DuplicateSubmissionException(String username, Long quizId) {
        super(String.format("Errore! l'utente %s ha già inviato una risposta per il quiz con ID:%d", username, quizId));
    }
}
