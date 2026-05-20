package org.gfmanca.the_guillotine.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String username) {
        super(String.format("Errore! l'utente %s non trovato.", username));
    }
}
