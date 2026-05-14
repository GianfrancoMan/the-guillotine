package org.gfmanca.the_guillotine.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gfmanca.the_guillotine.domain.enums.UserRole;

import java.time.LocalDateTime;

/**
 * Represents a user entity in the system.
 * <p>
 * This entity maps to the "users" table and stores the core information required
 * to identify and authenticate a user, including a unique username, password,
 * assigned role, and creation timestamp. The creation timestamp is managed by
 * the database and cannot be modified after the entity is created.
 * <p>
 * Attributes:
 * - id: The unique identifier for the user. This value is auto-generated.
 * - username: The user's unique and required name used for identification within the system.
 * - password: The user's required password used for authentication.
 * - createdAt: The timestamp indicating when the user entity was created. It is set on insert and cannot be updated.
 * - role: The role assigned to the user in the system. Possible values are defined in the {@code UserRole} enum.
 * <p>
 * This entity is related to other domain entities, such as {@code Submission},
 * where it represents the user who made submissions in a quiz.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

}

