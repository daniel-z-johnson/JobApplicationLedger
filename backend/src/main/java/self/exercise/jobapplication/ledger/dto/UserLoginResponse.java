package self.exercise.jobapplication.ledger.dto;

import self.exercise.jobapplication.ledger.models.UserLogin;

import java.time.Instant;
import java.util.UUID;

public record UserLoginResponse(UUID id, String ipAddress, Instant loginAt) {
    public static UserLoginResponse from(UserLogin login) {
        return new UserLoginResponse(
                login.getId(),
                login.getIpAddress(),
                login.getLoginAt()
        );
    }
}
