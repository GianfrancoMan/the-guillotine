package org.gfmanca.the_guillotine.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * A data transfer object for authentication requests.
 *
 * This record encapsulates the credentials required for authentication,
 * specifically the username and password.
 *
 * Constraints:
 * - username: Must not be blank.
 * - password: Must not be blank.
 */
public record AuthenticationRequestDto(@NotBlank String username, @NotBlank String password) { }
