package org.gfmanca.the_guillotine.dto;

/**
 * A data transfer object representing the response for an authentication request.
 *
 * This record encapsulates the token generated upon successful authentication.
 * The token can be used to access protected resources within the application.
 *
 * Field:
 * - token: The authentication token, typically a JWT, issued to the authenticated user.
 */
public record AuthenticationResponseDto(String token) { }
