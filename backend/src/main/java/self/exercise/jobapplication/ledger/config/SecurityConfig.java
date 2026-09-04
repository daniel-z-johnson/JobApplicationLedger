package self.exercise.jobapplication.ledger.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
public class SecurityConfig {

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
