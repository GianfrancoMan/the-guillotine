package org.gfmanca.the_guillotine.service;

import org.gfmanca.the_guillotine.domain.entity.Quiz;
import org.gfmanca.the_guillotine.domain.enums.QuizStatus;
import org.gfmanca.the_guillotine.dto.QuizResponseDto;
import org.gfmanca.the_guillotine.exception.QuizNotFoundException;
import org.gfmanca.the_guillotine.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages quiz creation, status changes, correct answers, and DTO conversion.
 */
@Service
public class QuizService {

    private final QuizRepository quizRepository;

    public QuizService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    /**
     * Creates a new quiz with CLOSED status.
     *
     * @param name the quiz name
     * @return the created quiz as a DTO
     */
    @Transactional
    public QuizResponseDto createQuiz(String name) {

        Quiz quiz = new Quiz();

        quiz.setName(name);
        quiz.setStatus(QuizStatus.CLOSED);

        Quiz savedQuiz = quizRepository.save(quiz);

        return mapToDto(savedQuiz);
    }

    /**
     * Opens an existing quiz.
     *
     * @param quizId the quiz ID
     * @return the updated quiz as a DTO
     */
    @Transactional
    public QuizResponseDto openQuiz(Long quizId) {

        Quiz quiz = getQuizOrThrow(quizId);

        quiz.setStatus(QuizStatus.OPEN);

        return mapToDto(quiz);
    }

    /**
     * Closes an existing quiz.
     *
     * @param quizId the quiz ID
     * @return the updated quiz as a DTO
     */
    @Transactional
    public QuizResponseDto closeQuiz(Long quizId) {

        Quiz quiz = getQuizOrThrow(quizId);

        quiz.setStatus(QuizStatus.CLOSED);

        return mapToDto(quiz);
    }

    /**
     * Sets the normalized correct answer for a quiz.
     *
     * @param quizId the quiz ID
     * @param correctAnswer the correct answer value
     * @return the updated quiz as a DTO
     */
    @Transactional
    public QuizResponseDto setCorrectAnswer(Long quizId, String correctAnswer) {

        Quiz quiz = getQuizOrThrow(quizId);

        quiz.setCorrectAnswer(correctAnswer.trim().toLowerCase());

        return mapToDto(quiz);
    }

    /**
     * Finds a quiz or throws an exception if it does not exist.
     *
     * @param quizId the quiz ID
     * @return the found quiz
     */
    private Quiz getQuizOrThrow(Long quizId) {

        return quizRepository.findById(quizId).orElseThrow(() -> new QuizNotFoundException(quizId));
    
    }

    /**
     * Converts a quiz entity into a response DTO.
     *
     * @param quiz the quiz entity
     * @return the quiz response DTO
     */
    private QuizResponseDto mapToDto(Quiz quiz) {

        return new QuizResponseDto(
                quiz.getId(),
                quiz.getName(),
                quiz.getStatus(),
                quiz.getCorrectAnswer(),
                quiz.getCreatedAt()
        );
    }
}
