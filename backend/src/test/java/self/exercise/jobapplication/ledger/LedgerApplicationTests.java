package self.exercise.jobapplication.ledger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import self.exercise.jobapplication.ledger.repositories.UserRepo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class LedgerApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepo userRepo;

	@BeforeEach
	void clearUsers() {
		userRepo.deleteAll();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void loginCreatesPasswordFreeSessionThatSupportsMeAndLogout() throws Exception {
		mockMvc.perform(post("/u/register")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "email": "  USER@Example.COM  ",
							  "username": "example-user",
							  "password": "password123",
							  "confirmPassword": "password123"
							}
							"""))
				.andExpect(status().isOk());

		var loginResult = mockMvc.perform(post("/u/login")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "email": "user@example.com",
							  "password": "password123"
							}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("user@example.com"))
				.andExpect(jsonPath("$.username").value("example-user"))
				.andReturn();

		var sessionCookie = loginResult.getResponse().getCookie("SESSION");
		assertThat(sessionCookie).isNotNull();

		mockMvc.perform(get("/u/me").cookie(sessionCookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("user@example.com"))
				.andExpect(jsonPath("$.username").value("example-user"));

		mockMvc.perform(post("/u/logout").cookie(sessionCookie).with(csrf()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/u/me"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void registrationRejectsMismatchedPasswords() throws Exception {
		mockMvc.perform(post("/u/register")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "email": "user@example.com",
							  "username": "example-user",
							  "password": "password123",
							  "confirmPassword": "different-password"
							}
							"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Bad Request"))
				.andExpect(jsonPath("$.message").value("Request validation failed"))
				.andExpect(jsonPath("$.path").value("/u/register"))
				.andExpect(jsonPath("$.violations.request[0]").value(
						"Password and confirm password do not match"
				));

		assertThat(userRepo.count()).isZero();
	}

	@Test
	void registrationRejectsInvalidUsernameCharacters() throws Exception {
		mockMvc.perform(post("/u/register")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "email": "user@example.com",
							  "username": "Invalid Username!",
							  "password": "password123",
							  "confirmPassword": "password123"
							}
							"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Bad Request"))
				.andExpect(jsonPath("$.message").value("Request validation failed"))
				.andExpect(jsonPath("$.path").value("/u/register"))
				.andExpect(jsonPath("$.violations.username[0]").value(
						"Username may contain only lowercase letters, numbers, underscores, and hyphens"
				));

		assertThat(userRepo.count()).isZero();
	}

	@Test
	void registrationRejectsDuplicateNormalizedEmailAndUsername() throws Exception {
		String firstUser = """
				{
				  "email": "user@example.com",
				  "username": "example-user",
				  "password": "password123",
				  "confirmPassword": "password123"
				}
				""";

		mockMvc.perform(post("/u/register")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(firstUser))
				.andExpect(status().isOk());

		mockMvc.perform(post("/u/register")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "email": "  USER@EXAMPLE.COM ",
							  "username": "another-user",
							  "password": "password123",
							  "confirmPassword": "password123"
							}
							"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.error").value("Conflict"))
				.andExpect(jsonPath("$.message").value("An account with those details already exists"))
				.andExpect(jsonPath("$.path").value("/u/register"));

		mockMvc.perform(post("/u/register")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "email": "another@example.com",
							  "username": "example-user",
							  "password": "password123",
							  "confirmPassword": "password123"
							}
							"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("An account with those details already exists"));

		assertThat(userRepo.count()).isOne();
	}

	@Test
	void loginRejectsInvalidCredentialsWithoutCreatingSession() throws Exception {
		mockMvc.perform(post("/u/login")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "email": "missing@example.com",
							  "password": "wrong-password"
							}
							"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.error").value("Unauthorized"))
				.andExpect(jsonPath("$.message").value("Invalid email or password"))
				.andExpect(jsonPath("$.path").value("/u/login"))
				.andExpect(cookie().doesNotExist("SESSION"));
	}

}
