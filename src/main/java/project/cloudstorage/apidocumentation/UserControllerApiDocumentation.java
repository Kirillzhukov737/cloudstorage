package project.cloudstorage.apidocumentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import project.cloudstorage.dto.user.UserResponseDto;

@Tag(name = "User", description = "User information")
public interface UserControllerApiDocumentation {

    @Operation(summary = "Get current user")
    @ApiResponse(responseCode = "200", description = "Get current user")
    @ApiResponse(responseCode = "401", description = "User is not authorized")
    @ApiResponse(responseCode = "500", description = "Unknown error")
    ResponseEntity<UserResponseDto> getCurrentUser(Authentication authentication);
}