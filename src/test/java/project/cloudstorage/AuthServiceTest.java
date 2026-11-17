package project.cloudstorage;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import project.cloudstorage.dto.user.UserResponseDto;
import project.cloudstorage.dto.user.UserSignInOrSignUpRequestDto;
import project.cloudstorage.exceptions.user.UserAlreadyExistsException;
import project.cloudstorage.service.user.AuthService;
import project.cloudstorage.service.user.UserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    UserService userService;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    SecurityContextRepository securityContextRepository;

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    HttpServletResponse httpServletResponse;

    @InjectMocks
    AuthService authService;

    private final UserSignInOrSignUpRequestDto dto = new UserSignInOrSignUpRequestDto("Nidalee", "Maxrangearrow123");
    private final Authentication authentication = new UsernamePasswordAuthenticationToken("Nidalee", null);

    @AfterEach
    void clearUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void sighIn_Validation_Credentials(){
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        UserResponseDto userResponseDto = authService.signIn(dto, httpServletRequest, httpServletResponse);
        assertEquals("Nidalee", userResponseDto.username());

        ArgumentCaptor<Authentication> argumentCaptor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(argumentCaptor.capture());
        assertEquals("Nidalee", argumentCaptor.getValue().getName());
        assertEquals("Maxrangearrow123", argumentCaptor.getValue().getCredentials());
        assertFalse(argumentCaptor.getValue().isAuthenticated());

        ArgumentCaptor<SecurityContext> securityContextArgumentCaptor = ArgumentCaptor.forClass(SecurityContext.class);
        verify(securityContextRepository).saveContext(securityContextArgumentCaptor.capture(), eq(httpServletRequest), eq(httpServletResponse));
        assertSame(authentication, securityContextArgumentCaptor.getValue().getAuthentication());
    }

    @Test
    void signIn_with_wrong_credentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("wrong credentials"));
        assertThrows(BadCredentialsException.class, () -> authService.signIn(dto, httpServletRequest, httpServletResponse));
        verifyNoInteractions(securityContextRepository);
        verify(httpServletRequest, never()).changeSessionId();
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void signUp_When_User_already_exists() {
        when(userService.register(dto)).thenThrow(new UserAlreadyExistsException("User already exists"));
        assertThrows(UserAlreadyExistsException.class, () -> authService.signUp(dto, httpServletRequest, httpServletResponse));
        verifyNoInteractions(authenticationManager,securityContextRepository);
    }
}
