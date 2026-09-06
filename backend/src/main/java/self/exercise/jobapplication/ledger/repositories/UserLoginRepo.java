package self.exercise.jobapplication.ledger.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import self.exercise.jobapplication.ledger.models.UserLogin;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserLoginRepo extends CrudRepository<UserLogin, UUID> {
    Page<UserLogin> findByUserIdOrderByLoginAtDesc(UUID userId, Pageable pageable);
    List<UserLogin> findAllByUserIdOrderByLoginAtDesc(UUID userId, Pageable pageable);
}
