package self.exercise.jobapplication.ledger.dto;

import java.time.Instant;

public record UserDTO(String email, String username, Instant createdAt, Instant updatedAt) {
}
