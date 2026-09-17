package self.exercise.jobapplication.ledger;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import self.exercise.jobapplication.ledger.dto.CompanyRequest;
import self.exercise.jobapplication.ledger.models.Company;
import self.exercise.jobapplication.ledger.models.User;
import self.exercise.jobapplication.ledger.repositories.UserRepo;
import self.exercise.jobapplication.ledger.services.CompanyService;

import javax.sql.DataSource;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
class CompanyVersionTests {
    @Autowired private EntityManagerFactory entityManagerFactory;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private CompanyService service;
    @Autowired private UserRepo userRepo;
    @Autowired private DataSource dataSource;

    @Test
    void overlappingTransactionsCannotOverwriteCommittedChanges() {
        var transactions = new TransactionTemplate(transactionManager);
        UUID owner = transactions.execute(status -> {
            User user = new User();
            user.setUsername("version-" + UUID.randomUUID());
            user.setEmail(user.getUsername() + "@example.com");
            user.setPasswordHash("not-used-by-this-test");
            return userRepo.save(user).getId();
        });
        var created = service.createCompany(owner, new CompanyRequest("Acme", "EMPLOYER", null, null));
        var first = entityManagerFactory.createEntityManager();
        var second = entityManagerFactory.createEntityManager();
        try {
            first.getTransaction().begin();
            second.getTransaction().begin();
            Company firstCopy = first.find(Company.class, created.id());
            Company secondCopy = second.find(Company.class, created.id());
            firstCopy.setName("First edit");
            first.getTransaction().commit();
            secondCopy.setName("Second edit");
            assertThatThrownBy(second::flush).isInstanceOf(OptimisticLockException.class);
            second.getTransaction().rollback();
            var actual = service.findByIdAndUserId(created.id(), owner);
            assertThat(actual.name()).isEqualTo("First edit");
            assertThat(actual.version()).isEqualTo(created.version() + 1);
        } finally {
            if (first.getTransaction().isActive()) first.getTransaction().rollback();
            if (second.getTransaction().isActive()) second.getTransaction().rollback();
            first.close();
            second.close();
            service.deleteCompany(created.id(), owner);
            userRepo.deleteById(owner);
        }
    }

    @Test
    void v4BackfillsExistingCompaniesWithoutChangingTheirData() {
        String schema = "version_test_" + UUID.randomUUID().toString().replace("-", "");
        var jdbc = new JdbcTemplate(dataSource);
        try {
            Flyway.configure().dataSource(dataSource).schemas(schema).defaultSchema(schema)
                    .locations("classpath:db/migration").target("3").load().migrate();
            UUID userId = UUID.randomUUID();
            UUID companyId = UUID.randomUUID();
            jdbc.update("INSERT INTO " + schema + ".users (id, email, username, password_hash) VALUES (?, ?, ?, ?)",
                    userId, "migration@example.com", "migration", "not-used");
            jdbc.update("INSERT INTO " + schema + ".companies (id, user_id, name, company_type) VALUES (?, ?, ?, ?)",
                    companyId, userId, "Existing company", "EMPLOYER");
            Flyway.configure().dataSource(dataSource).schemas(schema).defaultSchema(schema)
                    .locations("classpath:db/migration").target("4").load().migrate();
            assertThat(jdbc.queryForObject("SELECT version FROM " + schema + ".companies WHERE id = ?",
                    Long.class, companyId)).isZero();
            assertThat(jdbc.queryForObject("SELECT name FROM " + schema + ".companies WHERE id = ?",
                    String.class, companyId)).isEqualTo("Existing company");
        } finally {
            jdbc.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
        }
    }
}
