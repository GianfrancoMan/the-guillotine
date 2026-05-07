package org.gfmanca.the_guillotine.repository;

import org.gfmanca.the_guillotine.domain.entity.Quiz;
import org.gfmanca.the_guillotine.domain.enums.QuizStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByStatus(QuizStatus status);
}
