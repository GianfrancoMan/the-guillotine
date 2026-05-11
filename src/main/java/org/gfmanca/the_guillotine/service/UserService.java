package org.gfmanca.the_guillotine.service;

import jakarta.persistence.EntityManager;
import org.gfmanca.the_guillotine.domain.entity.User;
import org.gfmanca.the_guillotine.dto.UserResponseDto;
import org.gfmanca.the_guillotine.exception.UsernameAlreadyExistsException;
import org.gfmanca.the_guillotine.exception.UserNotFoundException;
import org.gfmanca.the_guillotine.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
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

    //only one constructor, @Autowired is not required.
    public UserService(UserRepository userRepository, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.entityManager = entityManager;
    }

    /**
     * Creates a new user in the system with the provided username.
     * The username is normalized before being saved. If the username
     * already exists in the system, an exception is thrown.
     *
     * @param username the desired username for the new user, which must be unique
     * @return a {@code UserResponseDto} containing the details of the newly created user
     * @throws UsernameAlreadyExistsException if a user with the given username already exists
     */
    @Transactional
    public UserResponseDto createUser(String username) {

        User user = new User();

        user.setUsername(normalizeUsername(username));

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
     * @throws UserNotFoundException if no user is found with the given ID
     */
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->  new UserNotFoundException(userId));

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
                user.getCreatedAt()
        );
    }
}
