package self.exercise.jobapplication.ledger.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CsrfSessionAuthenticationStrategyTests {

    @Test
    void successfulAuthenticationClearsExistingCsrfToken() {
        var repository = new RecordingCsrfTokenRepository();
        SessionAuthenticationStrategy strategy = new SecurityConfig()
                .sessionAuthenticationStrategy(repository);
        var request = new MockHttpServletRequest();
        request.getSession();

        strategy.onAuthentication(
                new TestingAuthenticationToken("user@example.com", null),
                request,
                new MockHttpServletResponse()
        );

        assertEquals(1, repository.savedTokens.size());
        assertNull(repository.savedTokens.getFirst());
    }

    private static final class RecordingCsrfTokenRepository implements CsrfTokenRepository {
        private final CsrfToken originalToken = token("pre-login-token");
        private final List<CsrfToken> savedTokens = new ArrayList<>();

        @Override
        public CsrfToken generateToken(HttpServletRequest request) {
            return token("post-login-token");
        }

        @Override
        public void saveToken(
                CsrfToken token,
                HttpServletRequest request,
                HttpServletResponse response) {
            savedTokens.add(token);
        }

        @Override
        public CsrfToken loadToken(HttpServletRequest request) {
            return originalToken;
        }

        private static CsrfToken token(String value) {
            return new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", value);
        }
    }
}
