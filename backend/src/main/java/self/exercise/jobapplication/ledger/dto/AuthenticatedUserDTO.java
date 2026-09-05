package self.exercise.jobapplication.ledger.dto;

import self.exercise.jobapplication.ledger.security.UserPrincipal;

import java.util.UUID;

public record AuthenticatedUserDTO(UUID id, String email, String username) {

    public static AuthenticatedUserDTO from(UserPrincipal principal) {
        return new AuthenticatedUserDTO(
                principal.getId(),
                principal.getEmail(),
                principal.getDisplayName()
        );
    }
}
