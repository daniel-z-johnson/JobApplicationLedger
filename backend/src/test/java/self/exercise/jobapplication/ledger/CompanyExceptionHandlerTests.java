package self.exercise.jobapplication.ledger;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import self.exercise.jobapplication.ledger.exceptions.ApiExceptionHandler;
import self.exercise.jobapplication.ledger.exceptions.CompanyNotFoundException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CompanyExceptionHandlerTests {
    @Test
    void optimisticLockFailureIsRenderedAs409() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new ApiExceptionHandler()).build();
        mvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void companyNotFoundIsRenderedAsStructured404() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new ApiExceptionHandler()).build();
        mvc.perform(get("/test/company"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Company not found"))
                .andExpect(jsonPath("$.path").value("/test/company"));
    }

    @RestController
    static class TestController {
        @GetMapping("/test/conflict")
        public void conflict() {
            throw new org.springframework.orm.ObjectOptimisticLockingFailureException(
                    self.exercise.jobapplication.ledger.models.Company.class, java.util.UUID.randomUUID());
        }

        @GetMapping("/test/company")
        public void company() {
            throw new CompanyNotFoundException();
        }
    }
}
