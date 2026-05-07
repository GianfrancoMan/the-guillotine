package org.gfmanca.the_guillotine.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super(String.format("Errore! utente con ID %d non trovato.", userId));
    }
}
