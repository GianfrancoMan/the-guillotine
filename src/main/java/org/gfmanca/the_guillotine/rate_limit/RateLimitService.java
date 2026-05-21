package org.gfmanca.the_guillotine.rate_limit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

import org.springframework.stereotype.Service;

import java.time.Duration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service class for managing rate limits.
 * This class provides methods to resolve rate limit buckets based on user identifiers.
 */
@Service
public class RateLimitService {
    /*
     * Memorizza buckets in memory.
     *
     * Key examples:
     * - IP address
     * - username
     */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Resolves the login bucket for a given user ip address.
     * If the bucket for this IP address does not exist, it is created and stored in the map.
     * @param ip the IP address of the user
     * @return the resolved rate limit bucket
     */
    public Bucket resolveLoginBucket(String ip ) {
        return buckets.computeIfAbsent("LOGIN_" + ip,  key -> createLoginBucket());
    }
    /**
     * Resolves the login bucket for a given user username.
     * If the bucket for this user does not exist, it is created and stored in the map.
     * @param username the unique identifier of the user
     * @return the resolved rate limit bucket
     */
    public Bucket resolveSubmissionBucket(String username) {
        return buckets.computeIfAbsent("SUBMISSION_" + username,  key -> createSubmissionBucket());
    }

    private Bucket createLoginBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));

        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createSubmissionBucket() {

        Bandwidth limit = Bandwidth.classic( 3, Refill.greedy(3, Duration.ofSeconds(1)));

        return Bucket.builder().addLimit(limit).build();
    }

    //to support rate limit testing
    public void clearBuckets() {
        buckets.clear();
    }
}
