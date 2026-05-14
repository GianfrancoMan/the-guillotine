package org.gfmanca.the_guillotine.security;

import org.gfmanca.the_guillotine.domain.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * An implementation of the {@link UserDetails} interface that adapts the {@link User} entity
 * for use in Spring Security's authentication and authorization processes.
 * <p>
 * This class wraps a {@link User} object and provides methods required by the {@link UserDetails}
 * interface, such as retrieving the username, password, and granted authorities associated
 * with the user.
 */
public class SecurityUser implements UserDetails {

    private final User user;
    private final String ROLE_PREFIX = "ROLE_";

    public SecurityUser(User user) {
        this.user = user;
    }

    /**
     * Retrieves the collection of granted authorities associated with the user.
     *
     * @return a collection of {@link GrantedAuthority} that represents the roles
     *         or authorities granted to the user. Each authority is prefixed with
     *         "ROLE_" followed by the user's role name.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(ROLE_PREFIX + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }
}
