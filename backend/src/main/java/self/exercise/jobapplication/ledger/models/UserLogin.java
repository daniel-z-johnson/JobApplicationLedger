package self.exercise.jobapplication.ledger.models;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.UUIDGenerator;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "logins")
public class UserLogin {
    @Id
    @Column(nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private User user;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "login_at", nullable = false)
    private Instant loginAt;

    @PrePersist
    public void prePersist() {
        if(this.id == null) {
            this.id = Generators.timeBasedEpochGenerator().generate();
        }
        if(this.loginAt == null) {
            this.loginAt = Instant.now();
        }
    }
}
