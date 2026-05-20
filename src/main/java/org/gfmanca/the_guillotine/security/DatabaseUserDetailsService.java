package org.gfmanca.the_guillotine.security;

import org.gfmanca.the_guillotine.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

/**
 * A Spring Security {@link UserDetailsService} implementation that retrieves user details
 * from a database using the {@link UserRepository}.
 *
 * This service is designed to fetch user information based on the provided username
 * and transform it into a {@link UserDetails} object for authentication and authorization
 * purposes. It utilizes the {@link SecurityUser} class to encapsulate the user data.
 *
 * The class depends on the {@link UserRepository} to perform database operations
 * for finding user records by username.
 */
@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public DatabaseUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return userRepository.findByUsername(username.toLowerCase().trim()).map(SecurityUser::new)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
