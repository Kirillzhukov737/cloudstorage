package project.cloudstorage.service.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import project.cloudstorage.dto.user.UserResponseDto;
import project.cloudstorage.dto.user.UserSignInOrSignUpRequestDto;

@Service
public class AuthService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthService(UserService userService, AuthenticationManager authenticationManager,
                       SecurityContextRepository securityContextRepository) {

        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    public UserResponseDto signUp(UserSignInOrSignUpRequestDto dto, HttpServletRequest request,
                                  HttpServletResponse response) {

        UserResponseDto registeredUser = userService.register(dto);
        Authentication authentication = authenticate(dto.username(), dto.password());
        saveAuthentication(authentication, request, response);

        return registeredUser;
    }

    public UserResponseDto signIn(UserSignInOrSignUpRequestDto dto, HttpServletRequest request, HttpServletResponse response) {

        Authentication authentication = authenticate(dto.username(), dto.password());
        saveAuthentication(authentication, request, response);

        return new UserResponseDto(authentication.getName());
    }

    private Authentication authenticate(String username, String password) {
        return authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(username, password));
    }

    private void saveAuthentication(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {

        if (request.getSession(false) != null) {
            request.changeSessionId();
        }

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        securityContextRepository.saveContext(securityContext, request, response);
    }
}