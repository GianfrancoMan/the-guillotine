package org.gfmanca.the_guillotine.rate_limit;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    //only one constructor, @Autowired is not required.
    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)  throws ServletException, IOException {

        String path = request.getRequestURI();

        /* LOGIN RATE LIMIT */
        if (path.startsWith("/api/auth/login")) {
            String ip = request.getRemoteAddr();
            Bucket bucket = rateLimitService.resolveLoginBucket(ip);

            if (!bucket.tryConsume(1)) {
                rejectRequest(response);
                return;
            }
        }

        /* SUBMISSION RATE LIMIT */
        if (path.startsWith("/api/submissions")) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                Bucket bucket = rateLimitService.resolveSubmissionBucket(username);

                if (!bucket.tryConsume(1)) {
                    rejectRequest(response);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void rejectRequest(HttpServletResponse response ) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        response.getWriter().write(
                """
                {
                    "status": 429,
                    "error": "Too Many Requests",
                    "message": "Rate limit exceeded"
                }
                """
        );
    }

}
