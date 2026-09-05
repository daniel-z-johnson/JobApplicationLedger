package self.exercise.jobapplication.ledger.dto;

import self.exercise.jobapplication.ledger.models.User;
import self.exercise.jobapplication.ledger.security.UserPrincipal;

import java.util.UUID;

public record UserResponse(UUID id, String email, String username) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getUsername());
    }

    public static UserResponse from(UserPrincipal principal) {
        return new UserResponse(
                principal.getId(),
                principal.getEmail(),
                principal.getDisplayName()
        );
    }
}
