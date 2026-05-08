package org.gfmanca.the_guillotine.controller;

import jakarta.validation.Valid;
import org.gfmanca.the_guillotine.domain.entity.Submission;
import org.gfmanca.the_guillotine.dto.SubmissionRequestDto;
import org.gfmanca.the_guillotine.dto.SubmissionResponseDto;
import org.gfmanca.the_guillotine.dto.WinnerResponseDto;
import org.gfmanca.the_guillotine.service.SubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling submission-related HTTP requests.
 * Provides endpoints to create quiz submissions and determine
 * the winning submission for a quiz using the correct answer.
 */
@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    /**
     * Submits an answer to a quiz and returns the details of the created submission.
     *
     * This method processes the provided answer for a quiz by a user, creates a submission
     * record, and returns a data transfer object containing the details of the newly created submission.
     *
     * @param request the {@code SubmissionRequestDto} containing the necessary information to create
     *                a new submission, including the quiz ID, user ID, and the submitted answer.
     *                This parameter must be valid and non-null.
     * @return a {@code ResponseEntity} containing a {@code SubmissionResponseDto} with the details
     *         of the created submission. The HTTP status code will be {@code 201 CREATED}.
     */
    @PostMapping
    public ResponseEntity<SubmissionResponseDto> submitAnswer(@Valid @RequestBody SubmissionRequestDto request) {

        Submission submission = submissionService.submitAnswer(request.quizId(), request.userId(), request.answer());

        SubmissionResponseDto response = mapToDto(submission);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    private SubmissionResponseDto mapToDto(Submission submission) {

        return new SubmissionResponseDto(
                submission.getId(),
                submission.getQuiz().getId(),
                submission.getUser().getId(),
                submission.getAnswer(),
                submission.getSubmittedAt()
        );
    }

    @GetMapping("/winner")
    public ResponseEntity<WinnerResponseDto> findWinner(@RequestParam Long quizId, @RequestParam String correctAnswer) {

        WinnerResponseDto response = submissionService.findWinner( quizId, correctAnswer);

        return ResponseEntity.ok(response);
    }
}
