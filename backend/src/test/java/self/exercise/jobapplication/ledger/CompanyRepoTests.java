package self.exercise.jobapplication.ledger;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import self.exercise.jobapplication.ledger.models.Company;
import self.exercise.jobapplication.ledger.models.User;
import self.exercise.jobapplication.ledger.repositories.CompanyRepo;
import self.exercise.jobapplication.ledger.repositories.UserRepo;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@Transactional
class CompanyRepoTests {
    @Autowired
    private CompanyRepo companyRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private EntityManager entityManager;

    @Test
    void listingPagesAndCountsOnlyTheOwnersCompanies() {
        User owner = createUser("owner");
        User other = createUser("other");
        createCompany(owner, "Charlie");
        createCompany(owner, "Alpha");
        createCompany(owner, "Bravo");
        createCompany(other, "Alpha");
        entityManager.flush();
        entityManager.clear();

        var sort = Sort.by("name", "id");
        var first = companyRepo.findAllByUserId(owner.getId(), PageRequest.of(0, 2, sort));
        var second = companyRepo.findAllByUserId(owner.getId(), PageRequest.of(1, 2, sort));

        assertThat(first.getContent()).extracting(Company::getName).containsExactly("Alpha", "Bravo");
        assertThat(first.getContent()).allSatisfy(company ->
                assertThat(company.getUser().getId()).isEqualTo(owner.getId()));
        assertThat(first.getTotalElements()).isEqualTo(3);
        assertThat(first.getTotalPages()).isEqualTo(2);
        assertThat(first.hasNext()).isTrue();
        assertThat(second.getContent()).extracting(Company::getName).containsExactly("Charlie");
        assertThat(second.hasNext()).isFalse();
        assertThat(companyRepo.findAllByUserId(UUID.randomUUID(), PageRequest.of(0, 2, sort))
                .getTotalElements()).isZero();
    }

    @Test
    void singleCompanyLookupsRequireTheMatchingOwner() {
        User owner = createUser("owner");
        User other = createUser("other");
        Company company = createCompany(owner, "Acme");
        entityManager.flush();
        entityManager.clear();

        assertThat(companyRepo.findByIdAndUserId(company.getId(), owner.getId()))
                .hasValueSatisfying(found -> assertThat(found.getName()).isEqualTo("Acme"));
        assertThat(companyRepo.findByIdAndUserId(company.getId(), other.getId())).isEmpty();
        assertThat(companyRepo.existsByIdAndUserId(company.getId(), owner.getId())).isTrue();
        assertThat(companyRepo.existsByIdAndUserId(company.getId(), other.getId())).isFalse();
        assertThat(companyRepo.findByIdAndUserId(UUID.randomUUID(), owner.getId())).isEmpty();
    }

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("not-used-by-this-test");
        return userRepo.save(user);
    }

    private Company createCompany(User user, String name) {
        Company company = new Company();
        company.setUser(user);
        company.setName(name);
        company.setCompanyType("EMPLOYER");
        return companyRepo.save(company);
    }
}
