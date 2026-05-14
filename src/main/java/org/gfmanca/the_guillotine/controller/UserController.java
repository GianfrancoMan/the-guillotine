package org.gfmanca.the_guillotine.controller;

import jakarta.validation.Valid;
import org.gfmanca.the_guillotine.dto.CreateUserRequestDto;
import org.gfmanca.the_guillotine.dto.UserResponseDto;
import org.gfmanca.the_guillotine.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller that provides endpoints for managing users.
 *
 * This controller handles HTTP requests for creating and retrieving user information,
 * leveraging the UserService for business logic.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    //only one constructor, @Autowired is not required.
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @Valid @RequestBody CreateUserRequestDto request) {

        UserResponseDto response = userService.createUser(request.username(), request.password());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long userId) {

        return ResponseEntity.ok(userService.getUserById(userId));
    }
}
