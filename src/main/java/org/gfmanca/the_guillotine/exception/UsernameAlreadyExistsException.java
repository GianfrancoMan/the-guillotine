package org.gfmanca.the_guillotine.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super(String.format("Utente %s già esistente.", username));
    }
}
