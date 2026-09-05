package self.exercise.jobapplication.ledger.security;

import org.junit.jupiter.api.Test;
import self.exercise.jobapplication.ledger.models.User;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserPrincipalTests {

    @Test
    void erasedPasswordIsNotSerializedWithPrincipal() throws Exception {
        var user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setUsername("example-user");
        user.setPasswordHash("password-hash-that-must-not-be-stored");

        var principal = UserPrincipal.from(user);
        principal.eraseCredentials();

        assertThat(principal.getPassword()).isNull();

        var serialized = new ByteArrayOutputStream();
        try (var output = new ObjectOutputStream(serialized)) {
            output.writeObject(principal);
        }

        assertThat(serialized.toString(StandardCharsets.ISO_8859_1))
                .doesNotContain("password-hash-that-must-not-be-stored");
    }
}
