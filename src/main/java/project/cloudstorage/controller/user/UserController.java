    package project.cloudstorage.controller.user;

    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.core.Authentication;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;
    import project.cloudstorage.apidocumentation.UserControllerApiDocumentation;
    import project.cloudstorage.dto.user.UserResponseDto;

    @RestController
    @RequestMapping("/api/user")
    public class UserController implements UserControllerApiDocumentation {

        @Override
        @GetMapping("/me")
        public ResponseEntity<UserResponseDto> getCurrentUser(Authentication authentication) {
            UserResponseDto currentUser = new UserResponseDto(authentication.getName());

            return ResponseEntity.status(HttpStatus.OK).body(currentUser);
        }
    }