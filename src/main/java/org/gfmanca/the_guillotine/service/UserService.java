package org.gfmanca.the_guillotine.service;

import jakarta.persistence.EntityManager;
import org.gfmanca.the_guillotine.domain.entity.User;
import org.gfmanca.the_guillotine.domain.enums.UserRole;
import org.gfmanca.the_guillotine.dto.UserResponseDto;
import org.gfmanca.the_guillotine.exception.UserIdNotFoundException;
import org.gfmanca.the_guillotine.exception.UsernameAlreadyExistsException;
import org.gfmanca.the_guillotine.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class responsible for managing user-related operations.
 *
 * This class provides methods for creating users and retrieving user details
 * based on unique identifiers. It interacts with the user repository and
 * entity manager to persist and retrieve data from the database. The class
 * ensures transactional consistency and handles exceptional cases such as
 * duplicate usernames or nonexistent users.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final PasswordEncoder passwordEncoder;

    //only one constructor, @Autowired is not required.
    public UserService(UserRepository userRepository, EntityManager entityManager, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.entityManager = entityManager;
        this.passwordEncoder = passwordEncoder;
    }

/**
 * Creates a new user in the system with the provided username and password.
 * The username is normalized before being saved, the password is encoded,
 * and the user is assigned the default {@code PLAYER} role. If the username
 * already exists in the system, an exception is thrown.
 *
 * @param username the desired username for the new user, which must be unique
 * @param password the raw password for the new user, which will be encoded before being saved
 * @return a {@code UserResponseDto} containing the details of the newly created user
 * @throws UsernameAlreadyExistsException if a user with the given username already exists
 */
@Transactional
public UserResponseDto createUser(String username, String password) {

        User user = new User();

        user.setUsername(normalizeUsername(username));
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(UserRole.PLAYER);

        try {
            User savedUser = userRepository.saveAndFlush(user);
            entityManager.refresh(savedUser);

            return mapToDto(savedUser);

        } catch (DataIntegrityViolationException ex) {
            throw new UsernameAlreadyExistsException(username);
        }
    }


    /**
     * Retrieves a user by their unique identifier.
     *
     * This method fetches the user from the database using the provided ID and converts
     * the user entity to a {@code UserResponseDto}. If no user is found with the given ID,
     * a {@code UserNotFoundException} is thrown.
     *
     * @param userId the unique identifier of the user to retrieve
     * @return a {@code UserResponseDto} containing the user's details
     * @throws UserIdNotFoundException if no user is found with the given ID
     */
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->  new UserIdNotFoundException(userId));

        return mapToDto(user);
    }

    /*
     * Normalizes the provided username by trimming leading and trailing
     * whitespace and converting all characters to lowercase.
     *
     * param: username the username to be normalized
     * return: the normalized username
     */
    private String normalizeUsername(String username) {

        return username.trim().toLowerCase();
    }

    /*
     * Converts a User entity to a UserResponseDto.
     *
     * This method maps the fields from the User entity to the corresponding
     * fields in the UserResponseDto. It includes the user's ID, username,
     * and the timestamp indicating when the user entity was created.
     *
     * param: user the User entity to be converted
     * return: a UserResponseDto containing the mapped data from the User entity
     */
    private UserResponseDto mapToDto(User user) {

        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
