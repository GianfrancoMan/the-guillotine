package org.gfmanca.the_guillotine.controller;

import jakarta.validation.Valid;
import org.gfmanca.the_guillotine.dto.CreateQuizRequestDto;
import org.gfmanca.the_guillotine.dto.QuizResponseDto;
import org.gfmanca.the_guillotine.service.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing administrative operations on quizzes.
 *
 * This controller provides endpoints for the following functionalities:
 * - Creating a new quiz.
 * - Opening an existing quiz to make it active.
 * - Closing an existing quiz to make it inactive.
 * - Setting the correct answer for an existing quiz.
 *
 * Endpoints are restricted to administrative users.
 */
@RestController
@RequestMapping("/api/admin/quizzes")
public class AdminQuizController {

    private final QuizService quizService;

    public AdminQuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    public ResponseEntity<QuizResponseDto> createQuiz(@Valid @RequestBody CreateQuizRequestDto request) {
        QuizResponseDto response = quizService.createQuiz(request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{quizId}/open")
    public ResponseEntity<QuizResponseDto> openQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.openQuiz(quizId));
    }

    @PatchMapping("/{quizId}/close")
    public ResponseEntity<QuizResponseDto> closeQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.closeQuiz(quizId));
    }

    @PatchMapping("/{quizId}/answer")
    public ResponseEntity<QuizResponseDto> setCorrectAnswer(@PathVariable Long quizId, @RequestParam String answer) {
        return ResponseEntity.ok(quizService.setCorrectAnswer(quizId, answer));
    }
}
