package project.cloudstorage.service.user;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.cloudstorage.dto.user.UserResponseDto;
import project.cloudstorage.dto.user.UserSignInOrSignUpRequestDto;
import project.cloudstorage.entity.User;
import project.cloudstorage.exceptions.user.UserAlreadyExistsException;
import project.cloudstorage.repository.jpa.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponseDto register(UserSignInOrSignUpRequestDto userSignInOrSignUpRequestDto) {

        String username = userSignInOrSignUpRequestDto.username();

        validateUserIsNotExists(username);

        String encodedPassword = passwordEncoder.encode(userSignInOrSignUpRequestDto.password());

        User user = new User(username, encodedPassword);

        try {
            User savedUser = userRepository.saveAndFlush(user);

            return new UserResponseDto(savedUser.getUsername());

        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExistsException("Username already exists");
        }
    }

    @Transactional(readOnly = true)
    public Integer getUserIdByUsername(String username) {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalStateException("User not found"));

        return user.getId();
    }

    private void validateUserIsNotExists(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists");
        }
    }
}