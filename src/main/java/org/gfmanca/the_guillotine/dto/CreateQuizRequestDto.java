package org.gfmanca.the_guillotine.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * A data transfer object representing a request to create a quiz.
 *
 * This record provides the necessary information required for creating a new quiz,
 * specifically the quiz name.
 *
 * Constraints:
 * - name: Must not be blank.
 */
public record CreateQuizRequestDto(@NotBlank String name) { }
