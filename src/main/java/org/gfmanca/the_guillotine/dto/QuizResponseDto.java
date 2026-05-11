package org.gfmanca.the_guillotine.dto;

import org.gfmanca.the_guillotine.domain.enums.QuizStatus;

import java.time.LocalDateTime;

/**
 * A data transfer object representing the response for a quiz.
 *
 * This record encapsulates the essential information about a quiz, including its unique identifier,
 * name, current status, correct answer, and the timestamp when it was created. It serves as a
 * response structure for transmitting quiz details to clients.
 *
 */
public record QuizResponseDto(
        Long id,
        String name,
        QuizStatus status,
        String correctAnswer,
        LocalDateTime createdAt) { }
