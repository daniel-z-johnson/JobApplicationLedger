package self.exercise.jobapplication.ledger.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import self.exercise.jobapplication.ledger.controllers.UserController;
import self.exercise.jobapplication.ledger.services.UserLoginService;
import self.exercise.jobapplication.ledger.services.UserService;
import self.exercise.jobapplication.ledger.web.ClientIpAddressResolver;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class SecurityConfigTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserLoginService userLoginService;

    @MockitoBean
    private ClientIpAddressResolver clientIpAddressResolver;

    @Test
    void csrfTokenIsPubliclyAvailable() throws Exception {
        mockMvc.perform(get("/u/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").isNotEmpty())
                .andExpect(jsonPath("$.parameterName").value("_csrf"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void spaCookieIsReadableOutsideApiAndAcceptedAsHeader() throws Exception {
        var result = mockMvc.perform(get("/api/u/csrf").contextPath("/api"))
                .andExpect(status().isOk())
                .andExpect(cookie().path("XSRF-TOKEN", "/"))
                .andExpect(cookie().httpOnly("XSRF-TOKEN", false))
                .andReturn();
        var tokenCookie = result.getResponse().getCookie("XSRF-TOKEN");

        mockMvc.perform(post("/api/u/register").contextPath("/api")
                        .cookie(tokenCookie)
                        .header("X-XSRF-TOKEN", tokenCookie.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"));

        mockMvc.perform(post("/api/u/register").contextPath("/api")
                        .cookie(tokenCookie)
                        .header("X-XSRF-TOKEN", "invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrationIsPublicWhenCsrfTokenIsPresent() throws Exception {
        mockMvc.perform(post("/u/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrationRequiresCsrfToken() throws Exception {
        mockMvc.perform(post("/u/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void loginRequiresCsrfToken() throws Exception {
        mockMvc.perform(post("/u/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void otherEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/u/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }
}
