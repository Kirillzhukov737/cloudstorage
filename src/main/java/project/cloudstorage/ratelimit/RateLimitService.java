package project.cloudstorage.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final Cache<String, Bucket> rateLimiterCache;
    private final RateLimitConfig rateLimitConfig;

    public RateLimitService(Cache<String, Bucket> rateLimiterCache, RateLimitConfig rateLimitConfig) {
        this.rateLimiterCache = rateLimiterCache;
        this.rateLimitConfig = rateLimitConfig;
    }

    public Bucket resolveBucket(String key) {
        return rateLimiterCache.get(key, ignoredKey -> rateLimitConfig.createBucket());
    }
}
