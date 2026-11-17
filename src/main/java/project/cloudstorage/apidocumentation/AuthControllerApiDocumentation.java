package project.cloudstorage.apidocumentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import project.cloudstorage.dto.user.UserResponseDto;
import project.cloudstorage.dto.user.UserSignInOrSignUpRequestDto;

@Tag(name = "Auth", description = "Sign-Up and Sign-In user")
public interface AuthControllerApiDocumentation {


    @Operation(summary = "SignUp user")
    @ApiResponse(responseCode = "201", description = "User was created")
    @ApiResponse(responseCode = "400", description = "Validation exception")
    @ApiResponse(responseCode = "409", description = "Username already exist")
    @ApiResponse(responseCode = "429",description = "To many requests")
    @ApiResponse(responseCode = "500", description = "Unknow error")
    ResponseEntity<UserResponseDto> signUp(UserSignInOrSignUpRequestDto dto, HttpServletRequest request,
                                           HttpServletResponse response);

    @Operation(summary = "SignIn user")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "400", description = "Validation exception")
    @ApiResponse(responseCode = "401", description = "Incorrect login or password")
    @ApiResponse(responseCode = "429",description = "To many requests")
    @ApiResponse(responseCode = "500", description = "Unknow error")
    ResponseEntity<UserResponseDto> signIn(UserSignInOrSignUpRequestDto dto,
                                           HttpServletRequest request, HttpServletResponse response);

    @Operation(summary = "SignOut user")
    @ApiResponse(responseCode = "204",description = "No content")
    @ApiResponse(responseCode = "401", description = "The request is being executed by an unauthorized user")
    @ApiResponse(responseCode = "500", description = "Unknow error")
    ResponseEntity<Void> signOut();
}