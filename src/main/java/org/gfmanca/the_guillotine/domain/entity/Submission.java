package org.gfmanca.the_guillotine.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents a submission entity in the system.
 *
 * This entity corresponds to the "submissions" table in the database and captures
 * the details of a user's attempt to answer a quiz. Each submission is uniquely
 * identified by its combination of a user and a quiz.
 *
 * Attributes:
 * - id: The unique identifier for the submission. This is auto-generated.
 * - answer: The user's answer to the quiz question. It cannot be null and has a maximum length of 100 characters.
 * - submittedAt: The timestamp indicating when the submission was made. This is immutable once set.
 * - quiz: The quiz to which the submission belongs. It is a mandatory many-to-one relationship with the {@code Quiz} entity.
 * - user: The user who made the submission. It is a mandatory many-to-one relationship with the {@code User} entity.
 *
 * Constraints:
 * - A unique constraint ensures that a user cannot submit multiple answers for the same quiz.
 *
 * Relationships:
 * - quiz: Many-to-One relation with the {@code Quiz} entity, representing the quiz this submission is associated with.
 * - user: Many-to-One relation with the {@code User} entity, representing the user who made the submission.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "submissions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_user_quiz", columnNames = {"quiz_id", "user_id"})
        }
)
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "answer", nullable = false, length = 100)
    private String answer;

    @Column(name = "submitted_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime submittedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "quiz_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_submission_quiz")
    )
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_submission_user")
    )
    private User user;
}
