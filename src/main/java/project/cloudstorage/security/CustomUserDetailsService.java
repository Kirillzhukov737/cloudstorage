package project.cloudstorage.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import project.cloudstorage.entity.User;
import project.cloudstorage.repository.jpa.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final String USER_ROLE = "USER";

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername()).password(user.getPassword()).roles(USER_ROLE).build();
    }
}