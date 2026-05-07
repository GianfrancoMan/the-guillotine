package org.gfmanca.the_guillotine.repository;

import org.gfmanca.the_guillotine.domain.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    boolean existsByQuizIdAndUserId(Long quizId, Long userId);

    Optional<Submission> findFirstByQuizIdAndAnswerOrderBySubmittedAtAscIdAsc(Long quizId, String answer);
}
