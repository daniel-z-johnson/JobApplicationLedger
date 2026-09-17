package self.exercise.jobapplication.ledger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import self.exercise.jobapplication.ledger.dto.CompanyRequest;
import self.exercise.jobapplication.ledger.exceptions.CompanyNotFoundException;
import self.exercise.jobapplication.ledger.models.User;
import self.exercise.jobapplication.ledger.repositories.UserRepo;
import self.exercise.jobapplication.ledger.services.CompanyService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@Transactional
class CompanyServiceTests {
    @Autowired private CompanyService service;
    @Autowired private UserRepo userRepo;

    @Test
    void ownerCanCreateListReadUpdateAndDeleteCompany() {
        UUID owner = createUser("owner");
        var created = service.createCompany(owner, new CompanyRequest(" Acme ", "EMPLOYER", " ", null));
        assertThat(created.name()).isEqualTo("Acme");
        assertThat(created.websiteUrl()).isNull();
        assertThat(created.createdAt()).isNotNull();
        assertThat(service.findByIdAndUserId(created.id(), owner).id()).isEqualTo(created.id());
        var page = service.findAllByUserId(owner, PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).extracting(item -> item.id()).containsExactly(created.id());

        var updated = service.updateCompany(created.id(), owner,
                new CompanyRequest("Acme Inc", "AGENCY", "https://example.com", null, created.version()));
        assertThat(updated.name()).isEqualTo("Acme Inc");
        assertThat(updated.version()).isEqualTo(created.version() + 1);
        assertThat(updated.websiteUrl()).isEqualTo("https://example.com");
        assertThat(updated.createdAt()).isEqualTo(created.createdAt());
        assertThat(updated.updatedAt()).isAfterOrEqualTo(created.updatedAt());

        service.deleteCompany(created.id(), owner);
        assertThatThrownBy(() -> service.findByIdAndUserId(created.id(), owner))
                .isInstanceOf(CompanyNotFoundException.class);
    }

    @Test
    void otherUserCannotReadEditOrDeleteCompany() {
        UUID owner = createUser("owner");
        UUID other = createUser("other");
        var created = service.createCompany(owner, request("Acme"));
        assertThat(service.findAllByUserId(other, PageRequest.of(0, 10)).getTotalElements()).isZero();
        assertThatThrownBy(() -> service.findByIdAndUserId(created.id(), other))
                .isInstanceOf(CompanyNotFoundException.class).hasMessage("Company not found");
        assertThatThrownBy(() -> service.updateCompany(created.id(), other, request("Changed")))
                .isInstanceOf(CompanyNotFoundException.class);
        assertThatThrownBy(() -> service.deleteCompany(created.id(), other))
                .isInstanceOf(CompanyNotFoundException.class);
        assertThat(service.findByIdAndUserId(created.id(), owner).name()).isEqualTo("Acme");
        assertThatThrownBy(() -> service.findByIdAndUserId(UUID.randomUUID(), owner))
                .isInstanceOf(CompanyNotFoundException.class).hasMessage("Company not found");
    }

    @Test
    void rejectsInvalidCompanyDetails() {
        UUID owner = createUser("owner");
        assertThatThrownBy(() -> service.createCompany(owner, request(" ")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.createCompany(owner,
                new CompanyRequest("Acme", "EMPLOYER", "javascript:alert(1)", null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void duplicateNamesProduceDatabaseConflictWithinServiceCall() {
        UUID owner = createUser("owner");
        service.createCompany(owner, request("Acme"));
        assertThatThrownBy(() -> service.createCompany(owner, request(" ACME ")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private UUID createUser(String name) {
        User user = new User();
        user.setUsername(name);
        user.setEmail(name + "@example.com");
        user.setPasswordHash("not-used-by-this-test");
        return userRepo.save(user).getId();
    }

    private static CompanyRequest request(String name) {
        return new CompanyRequest(name, "EMPLOYER", null, null);
    }
}
