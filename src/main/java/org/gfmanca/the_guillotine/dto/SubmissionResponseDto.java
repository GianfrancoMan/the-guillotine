package org.gfmanca.the_guillotine.dto;

import java.time.LocalDateTime;

/**
 * A data transfer object representing the response for a submitted quiz answer.
 *
 * This record encapsulates the details of a submission after it has been processed,
 * including the submission identifier, quiz identifier, user identifier, the provided answer,
 * and the timestamp indicating when the submission was made.
 *
 * Fields:
 * - submissionId: The unique identifier for the submission.
 * - quizId: The unique identifier for the associated quiz.
 * - userId: The unique identifier for the user who submitted the answer.
 * - answer: The answer submitted by the user.
 * - submittedAt: The timestamp representing when the submission occurred.
 */
public record SubmissionResponseDto(
        Long submissionId, Long quizId, Long userId, String answer, LocalDateTime submittedAt) {}
