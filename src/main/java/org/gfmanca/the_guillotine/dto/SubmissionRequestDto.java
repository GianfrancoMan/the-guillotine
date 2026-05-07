package org.gfmanca.the_guillotine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * A data transfer object representing a request for submitting an answer to a quiz.
 *
 * This record encapsulates the necessary information required to process a submission,
 * including the quiz identifier, user identifier, and the submitted answer.
 *
 * Fields:
 * - quizId: The unique identifier for the quiz.
 * - userId: The unique identifier for the user submitting the answer.
 * - answer: The answer provided by the user.
 *
 * Constraints:
 * - quizId: Must not be null.
 * - userId: Must not be null.
 * - answer: Must not be blank.
 */
public record SubmissionRequestDto(
        @NotNull Long quizId,
        @NotNull Long userId,
        @NotBlank @Size(max = 100) String answer) {

}
