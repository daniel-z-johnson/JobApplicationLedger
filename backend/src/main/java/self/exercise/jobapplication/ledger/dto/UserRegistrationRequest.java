package self.exercise.jobapplication.ledger.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import self.exercise.jobapplication.ledger.validation.PasswordsMatch;

import java.util.Locale;

@PasswordsMatch
public record UserRegistrationRequest(
        @Email @NotBlank @Size(max = 254) String email,
        @NotBlank @Size(max = 50) String username,
        @NotBlank @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters") String password,
        @NotBlank @Size(max = 72) String confirmPassword) {

    public UserRegistrationRequest {
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }
    }
}
