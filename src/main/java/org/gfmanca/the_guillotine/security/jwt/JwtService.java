package org.gfmanca.the_guillotine.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * Service component for handling operations related to JSON Web Tokens (JWT).
 * This class provides methods for generating, validating, and extracting
 * information from JWTs.
 */
@Service
public class JwtService {

    @Value("${spring.security.jwt.secret}")
    private String secret;
    @Value("${spring.security.jwt.expiration}")
    private long expiration;

    /**
     * Generates a JSON Web Token (JWT) for the specified user details.
     * The token includes the username as its subject, the current timestamp as the issue date,
     * and an expiration date determined by the configured expiration period.
     * The token is signed using a secret key and the HS256 signature algorithm.
     *
     * @param userDetails the user details for whom the token is generated,
     *                    containing a username or other identifying information.
     * @return the generated JWT as a string.
     */
    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey(),SignatureAlgorithm.HS256).compact();
    }

    /**
     * Extracts the username from the specified JWT token.
     * The username is retrieved from the subject field of the token's claims.
     *
     * @param token the JWT token from which the username is to be extracted.
     * @return the username contained in the subject field of the token's claims.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Validates the provided JSON Web Token (JWT) by checking if the username contained within
     * the token matches the username from the provided user details and ensures that the token
     * is not expired.
     *
     * @param token the JWT to be validated.
     * @param userDetails the user details containing the username to match with the username in the token.
     * @return {@code true} if the token is valid and not expired; {@code false} otherwise.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Checks if the provided JSON Web Token (JWT) is expired.
     * This method extracts the expiration date from the token and compares it with the current date.
     *
     * @param token the JWT to be checked for expiration.
     * @return {@code true} if the token is expired; {@code false} otherwise.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from the provided JSON Web Token (JWT).
     * The expiration date is retrieved from the token's claims.
     *
     * @param token the JWT from which the expiration date is to be extracted.
     * @return the expiration date of the token as a {@code Date} object.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a claim from the provided JSON Web Token (JWT) by applying a resolver function
     * to the token's claims. The resolver function determines which specific claim to extract.
     *
     * @param <T> the type of the claim to be extracted.
     * @param token the JWT from which the claim is to be extracted.
     * @param resolver a function that extracts the desired claim from the token's claims.
     * @return the value of the claim extracted using the provided resolver function.
     */
    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims =  extractAllClaims(token);
        return resolver.apply(claims);
    }

    /*
     * Extracts all claims from the provided JSON Web Token (JWT).
     * The claims are retrieved by parsing and validating the token using the configured signing key.
     *
     * param: token the JWT from which the claims are to be extracted.
     * return: the claims contained in the token as a Claims object.
     */
    private Claims extractAllClaims(String token) {

        return Jwts.parser().verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /*
     * Derives the secret signing key to be used for signing JSON Web Tokens (JWTs).
     * The signing key is generated by decoding a Base64-encoded secret and creating an HMAC SHA key.
     *
     * return the secret key used for signing JWTs.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
