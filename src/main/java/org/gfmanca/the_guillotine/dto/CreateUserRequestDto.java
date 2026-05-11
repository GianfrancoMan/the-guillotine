package org.gfmanca.the_guillotine.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequestDto(@NotBlank String username) {
}
