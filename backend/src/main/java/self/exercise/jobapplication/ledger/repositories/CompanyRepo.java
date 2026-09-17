package self.exercise.jobapplication.ledger.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;
import self.exercise.jobapplication.ledger.models.Company;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepo extends Repository<Company, UUID> {
    <S extends Company> S save(S company);

    void delete(Company company);

    <S extends Company> S saveAndFlush(S company);

    // Supply a stable sort, such as name followed by id, in the Pageable.
    Page<Company> findAllByUserId(UUID userId, Pageable pageable);

    Optional<Company> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);
}
