package org.gfmanca.the_guillotine.service;

import jakarta.persistence.EntityManager;
import org.gfmanca.the_guillotine.domain.entity.Quiz;
import org.gfmanca.the_guillotine.domain.entity.Submission;
import org.gfmanca.the_guillotine.domain.entity.User;
import org.gfmanca.the_guillotine.domain.enums.QuizStatus;
import org.gfmanca.the_guillotine.dto.WinnerResponseDto;
import org.gfmanca.the_guillotine.exception.*;
import org.gfmanca.the_guillotine.repository.QuizRepository;
import org.gfmanca.the_guillotine.repository.SubmissionRepository;
import org.gfmanca.the_guillotine.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Service class for handling quiz submission and winner resolution logic.
 * This service manages answer submissions, validates quiz availability,
 * normalizes submitted answers, handles duplicate submission constraints,
 * and determines the first user who submitted the correct answer for a quiz.
 */
@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    //@Autowired is not required because there is only one constructor and injection is done by Spring.
    public SubmissionService
        (SubmissionRepository submissionRepository, QuizRepository quizRepository, UserRepository userRepository, EntityManager entityManager) {
        this.submissionRepository = submissionRepository;
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
    }

    /**
     * Submits a user's answer for a given quiz.
     * Ensures that the quiz is available and open for submissions before saving the answer.
     * Handles duplicate submissions and validates constraints.
     *
     * @param quizId the identifier of the quiz for which the answer is being submitted
     * @param userId the identifier of the user submitting the answer
     * @param answer the answer provided by the user
     * @return the saved {@code Submission} entity containing details of the answer submission
     * @throws QuizNotFoundException if the quiz with the given ID does not exist
     * @throws UserNotFoundException if the user with the given ID does not exist
     * @throws QuizClosedException if the quiz is not open or outside its allowed submission window
     * @throws DuplicateSubmissionException if the user has already submitted an answer for the quiz
     * @throws DataIntegrityViolationException if there is a violation of database constraints
     */
    @Transactional
    public Submission submitAnswer(Long quizId, Long userId, String answer) {

        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new QuizNotFoundException(quizId));

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        validateQuizAvailability(quiz);

        Submission submission = new Submission();
        submission.setQuiz(quiz);
        submission.setUser(user);
        submission.setAnswer(normalizeAnswer(answer));

        try {

            Submission savedSubmission = submissionRepository.saveAndFlush(submission);

            entityManager.refresh(savedSubmission);

            return savedSubmission;

        } catch (DataIntegrityViolationException ex) {

            if (isUniqueConstraintViolation(ex)) {
                throw new DuplicateSubmissionException(user.getUsername(), quizId);
            }

            throw ex;
        }
    }

    /*
     * Validates the availability of a quiz for participation.
     * Ensures that the quiz is open and within the valid time window for submissions.
     * Throws an exception if the quiz is closed or the current time is outside the valid timeframe.
     *
     * param: quiz the quiz entity to validate, which contains information about its status, start time, and end time
     * throws: QuizClosedException if the quiz is not open for submissions or the current time is outside the start and end time of the quiz
     */
    private void validateQuizAvailability(Quiz quiz) {

        if (quiz.getStatus() != QuizStatus.OPEN) {
            throw new QuizClosedException(quiz.getId());
        }
    }

    /*
     * Normalizes the provided answer by trimming leading and trailing whitespace
     * and converting all characters to lowercase.
     *
     * param: answer the answer to be normalized
     * return: the normalized answer as a lowercase string with no leading or trailing whitespace
     */
    private String normalizeAnswer(String answer) {

        return answer.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Checks if the given DataIntegrityViolationException is caused by a violation
     * of the unique constraint "uq_user_quiz".
     *
     * param ex, the DataIntegrityViolationException to evaluate
     * return true if the exception message contains "uq_user_quiz", indicating
     *            a unique constraint violation; {@code false} otherwise
     */
    private boolean isUniqueConstraintViolation(DataIntegrityViolationException ex) {

        return ex.getMessage() != null && ex.getMessage().contains("uq_user_quiz");
    }

    /**
     * Finds and returns the winner of a quiz based on the first correct submission.
     * The method retrieves the quiz by its ID and determines the first user
     * who submitted the correct answer, ordered by submission time and ID.
     *
     * @param quizId the identifier of the quiz for which the winner is being determined
     * @return a {@code WinnerResponseDto} containing the winner's information, including the quiz ID,
     *         correct answer, winner's user ID, username, submission ID, and submission timestamp
     * @throws QuizNotFoundException if the quiz with the given ID does not exist
     * @throws CorrectAnswerNotSetException if the correct answer for the quiz has not been set
     * @throws NoWinnerFoundException if no submission matches the correct answer for the quiz
     */
    @Transactional(readOnly = true)
    public WinnerResponseDto findWinner(Long quizId) {

        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new QuizNotFoundException(quizId));

        if (quiz.getCorrectAnswer() == null || quiz.getCorrectAnswer().isBlank()) {
            throw new CorrectAnswerNotSetException(quizId);
        }

        Submission winningSubmission =
                submissionRepository.findFirstByQuizIdAndAnswerOrderBySubmittedAtAscIdAsc(quizId, quiz.getCorrectAnswer())
                        .orElseThrow(() -> new NoWinnerFoundException(quizId,  quiz.getCorrectAnswer()));

        return new WinnerResponseDto(
                quiz.getId(),
                quiz.getCorrectAnswer(),
                winningSubmission.getUser().getId(),
                winningSubmission.getUser().getUsername(),
                winningSubmission.getId(),
                winningSubmission.getSubmittedAt()
        );
    }
}
