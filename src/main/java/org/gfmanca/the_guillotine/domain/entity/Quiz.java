package org.gfmanca.the_guillotine.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gfmanca.the_guillotine.domain.enums.QuizStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * Represents a quiz entity in the system.
 *
 * This entity corresponds to the "quizzes" table in the database and includes
 * information such as the quiz's name, status, correct answer, and additional
 * metadata. A quiz can have multiple submissions by different users and is
 * associated with a specific lifecycle status.
 *
 * Attributes:
 * - id: The unique identifier for the quiz. This is auto-generated.
 * - name: The name or title of the quiz. It must be unique and cannot exceed 255 characters.
 * - status: The current lifecycle status of the quiz. Possible values are
 *           defined in the {@code QuizStatus} enum.
 * - correctAnswer: The correct answer to the quiz question, if applicable.
 * - createdAt: The timestamp indicating when the quiz was created. This is set
 *              automatically and cannot be modified.
 * - submissions: A collection of submissions associated with the quiz. Each submission
 *                represents an attempt made by a user to answer the quiz.
 *
 * Relationships:
 * - submissions: One-to-Many relationship with the {@code Submission} entity.
 *
 * This entity is used to manage quizzes and their associated details in the system.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private QuizStatus status;

    @Column(name = "correct_answer", length = 100)
    private String correctAnswer;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY)
    private List<Submission> submissions = new ArrayList<>();
}
