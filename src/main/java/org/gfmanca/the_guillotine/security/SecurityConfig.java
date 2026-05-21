package org.gfmanca.the_guillotine.security;

import org.gfmanca.the_guillotine.rate_limit.RateLimitFilter;
import org.gfmanca.the_guillotine.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configures the security settings for the application.
 * It defines beans necessary for securing HTTP requests and
 * handling password encoding mechanisms.
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;

    //@Autowired is not required because there is only one constructor and injection is done by Spring.
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, RateLimitFilter rateLimitFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    /**
     * Configures and provides a {@link SecurityFilterChain} bean to establish security rules
     * and filters for incoming requests. This configuration enables HTTP Basic authentication
     * and defines access control rules.
     *
     * @param http the {@link HttpSecurity} object used to customize security configurations.
     * @return the configured {@link SecurityFilterChain} instance.
     * @throws Exception if an error occurs during the configuration process.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/users/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rateLimitFilter, JwtAuthenticationFilter.class);



        return http.build();
    }

    /**
     * Provides a {@link PasswordEncoder} bean to handle password encoding
     * using the BCrypt hashing algorithm. This bean is typically used
     * for encoding raw passwords and verifying encoded passwords in
     * authentication processes.
     *
     * @return a {@link PasswordEncoder} instance that uses the BCrypt hashing algorithm.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Provides an {@link AuthenticationManager} bean for managing authentication processes.
     * This bean is configured based on the application's {@link AuthenticationConfiguration}.
     *
     * @param configuration the {@link AuthenticationConfiguration} object used to define the
     *                       authentication setup for the application.
     * @return an instance of {@link AuthenticationManager} configured according to the provided
     *         {@link AuthenticationConfiguration}.
     * @throws Exception if an error occurs while retrieving the {@link AuthenticationManager}.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
