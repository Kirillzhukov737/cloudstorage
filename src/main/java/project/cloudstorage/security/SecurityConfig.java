package project.cloudstorage.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain configureSecurityFilterChain(HttpSecurity http,
                                                            SecurityContextRepository securityContextRepository,
                                                            JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint,
                                                            JsonLogoutSuccessHandler jsonLogoutSuccessHandler)
            throws Exception {

        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/auth/sign-up",
                                "/api/auth/sign-in",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll().anyRequest().authenticated())

                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(jsonAuthenticationEntryPoint))

                .securityContext(securityContext -> securityContext
                        .securityContextRepository(securityContextRepository).requireExplicitSave(true))

                .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy.IF_REQUIRED))

                .formLogin(formLogin -> formLogin.disable())

                .httpBasic(basic -> basic.disable())

                .logout(logout -> logout.logoutRequestMatcher(PathPatternRequestMatcher
                                .withDefaults().matcher(HttpMethod.POST, "/api/auth/sign-out"))
                        .invalidateHttpSession(true).clearAuthentication(true)
                        .logoutSuccessHandler(jsonLogoutSuccessHandler));

        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
}