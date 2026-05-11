package org.gfmanca.the_guillotine.dto;

import java.time.LocalDateTime;

public record UserResponseDto(Long id, String username, LocalDateTime createdAt) {
}
