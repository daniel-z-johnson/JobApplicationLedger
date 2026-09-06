package self.exercise.jobapplication.ledger.dto;

import self.exercise.jobapplication.ledger.models.UserLogin;
import self.exercise.jobapplication.ledger.security.UserPrincipal;

import java.util.List;
import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        String email,
        String username,
        List<UserLoginResponse> recentLogins) {

    public static CurrentUserResponse from(UserPrincipal principal, List<UserLogin> recentLogins) {
        return new CurrentUserResponse(
                principal.getId(),
                principal.getEmail(),
                principal.getDisplayName(),
                recentLogins.stream().map(UserLoginResponse::from).toList()
        );
    }
}
