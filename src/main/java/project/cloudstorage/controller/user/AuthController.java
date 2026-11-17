package project.cloudstorage.controller.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.cloudstorage.apidocumentation.AuthControllerApiDocumentation;
import project.cloudstorage.dto.user.UserResponseDto;
import project.cloudstorage.dto.user.UserSignInOrSignUpRequestDto;
import project.cloudstorage.service.user.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthControllerApiDocumentation {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    @PostMapping("/sign-up")
    public ResponseEntity<UserResponseDto> signUp(@Valid @RequestBody UserSignInOrSignUpRequestDto dto,
                                                  HttpServletRequest request, HttpServletResponse response) {

        UserResponseDto registeredUser = authService.signUp(dto, request, response);

        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    @Override
    @PostMapping("/sign-in")
    public ResponseEntity<UserResponseDto> signIn(@Valid @RequestBody UserSignInOrSignUpRequestDto dto,
                                                  HttpServletRequest request, HttpServletResponse response) {

        UserResponseDto authenticatedUser = authService.signIn(dto, request, response);

        return ResponseEntity.status(HttpStatus.OK).body(authenticatedUser);
    }

    @Override
    @PostMapping("/sign-out")
    public ResponseEntity<Void> signOut(){
        throw new IllegalStateException("Something went wrong");
    }
}