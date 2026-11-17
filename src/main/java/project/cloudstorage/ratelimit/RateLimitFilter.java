package project.cloudstorage.ratelimit;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import project.cloudstorage.dto.error.ErrorResponseDto;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String SIGN_UP_PATH = "/api/auth/sign-up";
    private static final String SIGN_IN_PATH = "/api/auth/sign-in";

    private static final String RATE_LIMIT_ERROR_MESSAGE = "Too many requests";

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RateLimitService rateLimitService, ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String bucketKey = createBucketKey(request);
        Bucket bucket = rateLimitService.resolveBucket(bucketKey);

        boolean requestAllowed = bucket.tryConsume(1);

        if (requestAllowed) {
            filterChain.doFilter(request, response);
            return;
        }

        sendTooManyRequestsResponse(response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        boolean isPostRequest = HttpMethod.POST.matches(request.getMethod());
        boolean isRateLimitedPath = isRateLimitedPath(request.getRequestURI());
        boolean shouldApplyRateLimit = isPostRequest && isRateLimitedPath;

        return !shouldApplyRateLimit;
    }

    private String createBucketKey(HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        String requestPath = request.getRequestURI();

        String bucketKey = clientIp + ":" + requestPath;
        return bucketKey;
    }

    private void sendTooManyRequestsResponse(HttpServletResponse response)
            throws IOException {

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getWriter(), new ErrorResponseDto(RATE_LIMIT_ERROR_MESSAGE));
    }

    private boolean isRateLimitedPath(String requestPath) {
        return SIGN_UP_PATH.equals(requestPath) || SIGN_IN_PATH.equals(requestPath);
    }
}
