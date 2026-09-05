package self.exercise.jobapplication.ledger.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record UserLoginRequest(
        @Email @NotBlank @Size(max = 254) String email,
        @NotBlank @Size(max = 72) String password) {

    public UserLoginRequest {
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }
    }
}
