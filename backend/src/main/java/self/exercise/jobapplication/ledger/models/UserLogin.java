package self.exercise.jobapplication.ledger.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
    private User user;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "login_at", nullable = false)
    private Instant loginAt;
}
