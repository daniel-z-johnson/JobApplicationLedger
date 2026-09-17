package self.exercise.jobapplication.ledger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import self.exercise.jobapplication.ledger.dto.CompanyRequest;
import self.exercise.jobapplication.ledger.models.User;
import self.exercise.jobapplication.ledger.repositories.UserRepo;
import self.exercise.jobapplication.ledger.security.UserPrincipal;
import self.exercise.jobapplication.ledger.services.CompanyService;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CompanyControllerTests {
    private static final String VALID = """
            {"name":"Acme","companyType":"EMPLOYER","websiteUrl":"https://example.com"}
            """;
    @Autowired private MockMvc mvc;
    @Autowired private UserRepo userRepo;
    @Autowired private CompanyService service;
    @Autowired private ObjectMapper mapper;

    @Test
    void ownerCanCreateReadReplaceAndDelete() throws Exception {
        var owner = principal("owner");
        var result = mvc.perform(post("/companies").with(user(owner)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Acme"))
                .andExpect(jsonPath("$.user").doesNotExist())
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andReturn();
        String id = mapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
        mvc.perform(get("/companies/{id}", id).with(user(owner)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Acme"));
        org.assertj.core.api.Assertions.assertThat(result.getResponse().getHeader("Location"))
                .endsWith("/companies/" + id);
        mvc.perform(put("/companies/{id}", id).with(user(owner)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\",\"companyType\":\"AGENCY\",\"version\":0}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.websiteUrl").isEmpty());
        mvc.perform(delete("/companies/{id}", id).with(user(owner)).with(csrf()))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        mvc.perform(get("/companies/{id}", id).with(user(owner)))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Company not found"));
    }

    @Test
    void listIsPagedSortedBoundedAndOwnerScoped() throws Exception {
        var owner = principal("owner");
        var other = principal("other");
        create(owner, "Bravo");
        create(owner, "Alpha");
        create(other, "Aardvark");
        mvc.perform(get("/companies?page=0&size=1&sort=name,asc").with(user(owner)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Alpha"))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.totalPages").value(2));
        mvc.perform(get("/companies?page=1&size=1&sort=name,asc").with(user(owner)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].name").value("Bravo"));
        mvc.perform(get("/companies?size=500").with(user(owner)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.page.size").value(100));
        mvc.perform(get("/companies?sort=user.passwordHash").with(user(owner)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void otherUsersCannotReadEditOrDeleteCompany() throws Exception {
        var owner = principal("owner");
        var other = principal("other");
        UUID id = create(owner, "Acme");
        mvc.perform(get("/companies/{id}", id).with(user(other)))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Company not found"));
        mvc.perform(put("/companies/{id}", id).with(user(other)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/companies/{id}", id).with(user(other)).with(csrf()))
                .andExpect(status().isNotFound());
        mvc.perform(get("/companies/{id}", UUID.randomUUID()).with(user(other)))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Company not found"));
        mvc.perform(get("/companies/{id}", id).with(user(owner)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Acme"));
    }

    @Test
    void requiresAuthenticationAndCsrfForWrites() throws Exception {
        var owner = principal("owner");
        UUID id = create(owner, "Acme");
        mvc.perform(get("/companies")).andExpect(status().isUnauthorized());
        mvc.perform(get("/companies/{id}", id)).andExpect(status().isUnauthorized());
        mvc.perform(post("/companies").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/companies/{id}", id).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete("/companies/{id}", id).with(csrf())).andExpect(status().isUnauthorized());
        mvc.perform(post("/companies").with(user(owner)).contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isForbidden());
        mvc.perform(put("/companies/{id}", id).with(user(owner)).contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/companies/{id}", id).with(user(owner))).andExpect(status().isForbidden());
    }

    @Test
    void invalidBodiesAndIdsReturn400() throws Exception {
        var owner = principal("owner");
        mvc.perform(post("/companies").with(user(owner)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.violations.name").isArray())
                .andExpect(jsonPath("$.violations.companyType").isArray());
        mvc.perform(post("/companies").with(user(owner)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/companies/not-a-uuid").with(user(owner)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void staleUpdatesReturn409AndPreserveNewerData() throws Exception {
        var owner = principal("owner");
        UUID id = create(owner, "Acme");
        mvc.perform(get("/companies/{id}", id).with(user(owner)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.version").value(0));
        mvc.perform(put("/companies/{id}", id).with(user(owner)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New name\",\"companyType\":\"EMPLOYER\",\"version\":0}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.version").value(1));
        mvc.perform(put("/companies/{id}", id).with(user(owner)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Old form\",\"companyType\":\"EMPLOYER\",\"version\":0}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));
        mvc.perform(get("/companies/{id}", id).with(user(owner)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("New name"))
                .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    void updatesRequireNonnegativeVersion() throws Exception {
        var owner = principal("owner");
        UUID id = create(owner, "Acme");
        mvc.perform(put("/companies/{id}", id).with(user(owner)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/companies/{id}", id).with(user(owner)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Acme\",\"companyType\":\"EMPLOYER\",\"version\":-1}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.violations.version").isArray());
    }

    @Test
    void duplicateNameReturns409() throws Exception {
        var owner = principal("owner");
        create(owner, "ACME");
        mvc.perform(post("/companies").with(user(owner)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));
    }

    private UserPrincipal principal(String name) {
        User entity = new User();
        entity.setUsername(name);
        entity.setEmail(name + "@example.com");
        entity.setPasswordHash("not-used-by-this-test");
        return UserPrincipal.from(userRepo.save(entity));
    }

    private UUID create(UserPrincipal owner, String name) {
        return service.createCompany(owner.getId(), new CompanyRequest(name, "EMPLOYER", null, null)).id();
    }
}
