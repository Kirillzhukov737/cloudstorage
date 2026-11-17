package project.cloudstorage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import project.cloudstorage.dto.user.UserSignInOrSignUpRequestDto;
import project.cloudstorage.entity.User;
import project.cloudstorage.exceptions.user.UserAlreadyExistsException;
import project.cloudstorage.repository.jpa.UserRepository;
import project.cloudstorage.service.user.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    private final UserSignInOrSignUpRequestDto requestDto = new UserSignInOrSignUpRequestDto("Nidalee", "Maxrangearrow123");

    @Test
    void getUserId_ByUserName_ifUserExists_ReturnId() {
        User user = new User("Nidalee", "Maxrangearrow123");
        ReflectionTestUtils.setField(user, "id", 1);
        when(userRepository.findByUsername("Nidalee")).thenReturn(Optional.of(user));
        assertEquals(1, userService.getUserIdByUsername("Nidalee"));
    }

    @Test
    void getUserId_ByUserName_UserUnknow() {
        when(userRepository.findByUsername("Nidalee")).thenReturn(Optional.empty());
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                userService.getUserIdByUsername("Nidalee"));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void getDuplicateUser_WhenRegistration_newUser() {
        when(userRepository.findByUsername("Nidalee")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Maxrangearrow123")).thenReturn("Maxrangearrow123");
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));
        assertThrows(UserAlreadyExistsException.class, () -> userService.register(requestDto));
    }
}
