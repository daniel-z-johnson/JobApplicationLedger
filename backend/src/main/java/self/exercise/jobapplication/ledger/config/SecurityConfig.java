package self.exercise.jobapplication.ledger.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import self.exercise.jobapplication.ledger.dto.ApiErrorResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Slf4j
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository,
            CsrfTokenRepository csrfTokenRepository,
            ObjectMapper objectMapper) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/u/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/u/register", "/u/login").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf
                        .spa()
                        .csrfTokenRepository(csrfTokenRepository)
                )
                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> {
                            writeErrorResponse(
                                    response,
                                    objectMapper,
                                    HttpStatus.UNAUTHORIZED,
                                    "Authentication is required",
                                    request.getRequestURI()
                            );
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            writeErrorResponse(
                                    response,
                                    objectMapper,
                                    HttpStatus.FORBIDDEN,
                                    "Access denied",
                                    request.getRequestURI()
                            );
                        })
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return CookieCsrfTokenRepository.withHttpOnlyFalse();
    }

    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy(
            CsrfTokenRepository csrfTokenRepository) {
        return new CompositeSessionAuthenticationStrategy(List.of(
                new ChangeSessionIdAuthenticationStrategy(),
                new CsrfAuthenticationStrategy(csrfTokenRepository)
        ));
    }

    private static void writeErrorResponse(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            HttpStatus status,
            String message,
            String path) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getOutputStream(),
                ApiErrorResponse.of(status, message, path)
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        int defaultStrength = extractStrength(new BCryptPasswordEncoder().encode("startup-probe"))+2;
        var strength = defaultStrength + 2;

        log.info(
                "Using BCrypt strength {} (library default: {})",
                strength,
                defaultStrength
        );
        return new BCryptPasswordEncoder(strength);
    }

    private static int extractStrength(String bcryptHash) {
        String[] parts = bcryptHash.split("\\$");

        if (parts.length != 4 || parts[1].isBlank()) {
            throw new IllegalArgumentException("Unexpected BCrypt hash format");
        }

        return Integer.parseInt(parts[2]);
    }
}
