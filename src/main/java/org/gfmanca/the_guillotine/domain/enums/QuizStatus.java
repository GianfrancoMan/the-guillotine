package org.gfmanca.the_guillotine.domain.enums;

/**
 * Represents the various statuses a quiz can have during its lifecycle.
 *
 * The statuses capture the state of a quiz, which transitions as follows:
 *
 * - CREATED: Indicates that the quiz has been created but is not yet open for participation.
 * - OPEN: Indicates that the quiz is currently available for submissions.
 * - CLOSED: Indicates that the quiz is no longer accepting submissions.
 * - REVEALED: Indicates that the quiz's results or answers have been disclosed.
 */
public enum QuizStatus {
    CREATED,
    OPEN,
    CLOSED,
    REVEALED
}
