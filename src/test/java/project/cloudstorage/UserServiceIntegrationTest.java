package project.cloudstorage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import project.cloudstorage.config.PasswordConfig;
import project.cloudstorage.dto.user.UserSignInOrSignUpRequestDto;
import project.cloudstorage.entity.User;
import project.cloudstorage.exceptions.user.UserAlreadyExistsException;
import project.cloudstorage.repository.jpa.UserRepository;
import project.cloudstorage.service.user.UserService;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@Import({UserService.class, PasswordConfig.class})
public class UserServiceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void registerSaveUser() {

        var request = new UserSignInOrSignUpRequestDto("Nidalee", "maxrangearrow123");
        userService.register(request);
        assertTrue(userRepository.findByUsername("Nidalee").isPresent());
    }

    @Test
    void registerDuplicateUser() {
        var request = new UserSignInOrSignUpRequestDto("Nidalee", "maxrangearrow321");
        userService.register(request);
        Assertions.assertThrows(UserAlreadyExistsException.class, () -> {
            userService.register(request);
        });
    }

    @Test
    void registerChekPassword() {
        String password = "maxrangearrow123";
        var request = new UserSignInOrSignUpRequestDto("Nidalee", password);
        userService.register(request);
        var savedUser = userRepository.findByUsername(request.username()).orElseThrow();
        String savedPassword = savedUser.getPassword();
        assertNotEquals(password, savedPassword);
        assertTrue(passwordEncoder.matches(password, savedPassword));
        assertFalse(passwordEncoder.matches("wrong-password", savedPassword));
    }

    @Test
    void getUserByUsername() {
        var user = new User("Nidalee", passwordEncoder.encode("maxrangearrow123"));
        var saveUser = userRepository.saveAndFlush(user);
        var actualUserId = userService.getUserIdByUsername("Nidalee");
        assertEquals(saveUser.getId(), actualUserId);
    }

    @Test
    void getUserIdByUsernameIfUserNotExists() {
        assertThrows(IllegalStateException.class, () -> userService.getUserIdByUsername("User is not exists"));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void registerConcurrencyWithTheSameUsername() throws Exception {
        var request = new UserSignInOrSignUpRequestDto("Nidalee", "maxrangearrow123");
        var start = new CyclicBarrier(2);

        Callable<Boolean> callable = () -> {
            start.await();
            try {
                userService.register(request);
                return true;
            } catch (UserAlreadyExistsException e) {
                return false;
            }
        };

        try (var executor = Executors.newFixedThreadPool(2)) {
            var result = executor.invokeAll(List.of(callable, callable), 10, TimeUnit.SECONDS);
            assertNotEquals(result.get(0).get(), result.get(1).get());
            var savedUser = jdbcTemplate.queryForObject("SELECT COUNT (*)" +
                    "FROM users WHERE username =?", Integer.class, request.username());
            assertEquals(1, savedUser);
        } finally {
            jdbcTemplate.update("DELETE FROM users WHERE username =?", request.username());
        }
    }
}