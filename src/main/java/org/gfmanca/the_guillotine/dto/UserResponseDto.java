package org.gfmanca.the_guillotine.dto;

import org.gfmanca.the_guillotine.domain.enums.UserRole;

import java.time.LocalDateTime;

public record UserResponseDto(Long id, String username, UserRole role, LocalDateTime createdAt) {
}
