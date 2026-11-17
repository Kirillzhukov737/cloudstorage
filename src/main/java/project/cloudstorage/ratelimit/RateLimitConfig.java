package project.cloudstorage.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    private final long maxRequests;
    private final long windowSeconds;

    public RateLimitConfig(@Value("${rate-limit.auth.max-requests}") long maxRequests,
                           @Value("${rate-limit.auth.window-seconds}") long windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
    }

    @Bean
    public Cache<String, Bucket> rateLimiterCache() {
        return Caffeine.newBuilder().maximumSize(1000).expireAfterAccess(Duration.ofSeconds(600)).build();
    }

    public Bucket createBucket() {
        return Bucket.builder().addLimit(limit -> limit.capacity(maxRequests)
                .refillIntervally(maxRequests, Duration.ofSeconds(windowSeconds))).build();

    }
}
