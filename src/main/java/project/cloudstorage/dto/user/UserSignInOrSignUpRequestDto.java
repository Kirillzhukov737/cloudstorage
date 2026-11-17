package project.cloudstorage.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserSignInOrSignUpRequestDto(

        @NotBlank(message = "Username cannot be blank")
        @Size(min = 3, max = 50, message = "Username must contain between 3 and 50 characters")
        String username,

        @NotBlank(message = "Password cannot be blank")
        @Size(min = 3, max = 50, message = "Password must contain between 3 and 50 characters")
        String password) {
}