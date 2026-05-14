package org.gfmanca.the_guillotine.controller;

import jakarta.validation.Valid;
import org.gfmanca.the_guillotine.dto.AuthenticationRequestDto;
import org.gfmanca.the_guillotine.dto.AuthenticationResponseDto;
import org.gfmanca.the_guillotine.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDto> login(@Valid @RequestBody AuthenticationRequestDto request) {
        return ResponseEntity.ok(authenticationService .authenticate(request));
    }
}
