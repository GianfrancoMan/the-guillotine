package org.gfmanca.the_guillotine.service;
import org.gfmanca.the_guillotine.dto.AuthenticationRequestDto;
import org.gfmanca.the_guillotine.dto.AuthenticationResponseDto;
import org.gfmanca.the_guillotine.security.jwt.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Service responsible for handling authentication-related operations.
 *
 * This class provides a method to authenticate users based on their credentials
 * and generate a JWT token for use in subsequent secured communication.
 *
 * Dependencies:
 * - AuthenticationManager: Used to authenticate the user credentials.
 * - JwtService: Handles generation of JWT tokens.
 *
 * Behavior:
 * - The `authenticate` method validates the user's credentials using the
 *   AuthenticationManager.
 * - Upon successful authentication, the method generates a JWT token using
 *   the JwtService and returns it encapsulated in an AuthenticationResponseDto.
 */
@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthenticationResponseDto authenticate(AuthenticationRequestDto request ) {
        var authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        UserDetails user = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(user);

        return new AuthenticationResponseDto(token);
    }
}
