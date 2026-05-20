package org.gfmanca.the_guillotine.exception;

public class UserIdNotFoundException extends RuntimeException {
    public UserIdNotFoundException(Long userId) {
        super(String.format("Errore! l'utente con id %d non trovato.", userId));
    }
}
