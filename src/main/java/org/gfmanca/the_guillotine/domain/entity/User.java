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
 * This entity corresponds to the "users" table in the database and includes
 * fields for storing user-specific information such as a unique username,
 * user role, and the timestamp of when the user was created. The creation
 * timestamp is immutable once set.
 * <p>
 * Attributes:
 * - id: The unique identifier for the user. This is auto-generated.
 * - username: The name the user selects for identification within the system. It is unique and required.
 * - createdAt: The timestamp indicating when the user entity was created. It cannot be updated after being set.
 * - role: The role assigned to the user in the system. Possible values are defined in the {@code UserRole} enum (ADMIN, PLAYER).
 * <p>
 * This entity is related to other entities, such as {@code Submission}, where
 * it represents the user who made submissions in a quiz.
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

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

}

