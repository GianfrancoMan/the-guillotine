package org.gfmanca.the_guillotine.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.gfmanca.the_guillotine.security.DatabaseUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


/**
 * JwtAuthenticationFilter is a custom filter that extends Spring Security's OncePerRequestFilter
 * to handle authentication based on JSON Web Tokens (JWT).
 *
 * This filter extracts the JWT from the "Authorization" HTTP header, verifies its validity, and
 * sets the authentication details in the SecurityContext if the token is valid. The filter ensures
 * that the JWT is associated with the correct username and that the token has not expired.
 *
 * Responsibilities:
 * - Extracts the JWT from the "Authorization" header.
 * - Retrieves the username from the JWT using the JwtService.
 * - Loads user details from the database using the DatabaseUserDetailsService.
 * - Validates the JWT using the JwtService.
 * - Sets the authentication in the SecurityContext if the JWT is valid.
 *
 * Dependencies:
 * - JwtService: Provides methods to extract the username and validate the token.
 * - DatabaseUserDetailsService: Loads user details from the database.
 *
 * This filter is designed to be executed once per request and integrates with Spring Security's
 * authentication framework.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger =   LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final DatabaseUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, DatabaseUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Processes HTTP requests by applying JWT-based authentication.
     * Extracts the JWT token from the "Authorization" header, verifies it,
     * and sets the authenticated user in the Spring SecurityContext if the token is valid.
     * If the token is not present, invalid, or the request is already authenticated, it delegates
     * the processing to the next filter in the chain.
     *
     * @param request the current HTTP request being processed.
     * @param response the HTTP response corresponding to the current request.
     * @param filterChain the filter chain to pass the request and response to the next entity in the chain.
     * @throws ServletException if there is an internal error during processing.
     * @throws IOException if an I/O error occurs during request or response handling.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("No JWT found in request: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

            jwt = authHeader.substring(7);
            logger.debug("JWT received for request: {}", request.getRequestURI());
            username = jwtService.extractUsername(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            logger.debug("User loaded from DB: {}", username);

            boolean valid = jwtService.isTokenValid(jwt, userDetails);
            logger.debug("JWT validation result for user {}: {}", username, valid);
            if (valid) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);
                logger.debug("JWT authentication successful for user: {}", username);

            }
        } else if (SecurityContextHolder.getContext().getAuthentication() != null) {
            logger.debug("SecurityContext already contains authentication");
        }

        filterChain.doFilter(request, response);
        logger.debug("Filter chain completed for request: {}", request.getRequestURI());
    }

}
