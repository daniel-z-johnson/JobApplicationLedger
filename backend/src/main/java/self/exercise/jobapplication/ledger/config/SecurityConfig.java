package self.exercise.jobapplication.ledger.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

@Slf4j
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("determining strength to use");
        var strength = 10;
        var encoder = new BCryptPasswordEncoder(strength);
        Instant now = Instant.now();
        encoder.encode("test");
        while(Instant.now().isBefore(now.plusSeconds(1))) {
           ++strength;
            encoder = new BCryptPasswordEncoder(strength);
            now = Instant.now();
            encoder.encode("test");
        }
        log.info("Selected strength {}", strength);
        return new BCryptPasswordEncoder();
    }
}
