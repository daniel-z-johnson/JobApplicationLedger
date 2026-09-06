package self.exercise.jobapplication.ledger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import self.exercise.jobapplication.ledger.models.User;
import self.exercise.jobapplication.ledger.models.UserLogin;
import self.exercise.jobapplication.ledger.repositories.UserLoginRepo;
import self.exercise.jobapplication.ledger.repositories.UserRepo;
import self.exercise.jobapplication.ledger.security.UserPrincipal;
import self.exercise.jobapplication.ledger.services.UserLoginService;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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

	@Autowired
	private UserLoginRepo userLoginRepo;

	@MockitoSpyBean
	private UserLoginService userLoginService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void clearUsers() {
		reset(userLoginService);
		userRepo.deleteAll();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void loginHistoryIsPagedNewestFirst() {
		var user = new User();
		user.setEmail("history@example.com");
		user.setUsername("history-user");
		user.setPasswordHash("not-used-by-this-test");
		user = userRepo.save(user);

		Instant oldest = Instant.parse("2026-01-01T00:00:00Z");
		Instant middle = Instant.parse("2026-02-01T00:00:00Z");
		Instant newest = Instant.parse("2026-03-01T00:00:00Z");
		userLoginRepo.save(login(user, "192.0.2.1", oldest));
		userLoginRepo.save(login(user, "192.0.2.2", middle));
		userLoginRepo.save(login(user, "192.0.2.3", newest));

		var logins = userLoginRepo.findAllByUserIdOrderByLoginAtDesc(
				user.getId(),
				PageRequest.of(0, 2)
		);

		assertThat(logins)
				.extracting(UserLogin::getLoginAt)
				.containsExactly(newest, middle);
	}

	@Test
	void successfulLoginCanBeRecorded() {
		var user = new User();
		user.setEmail("recording@example.com");
		user.setUsername("recording-user");
		user.setPasswordHash("not-used-by-this-test");
		user = userRepo.save(user);

		var recordedLogin = userLoginService.recordSuccessfulLogin(
				UserPrincipal.from(user),
				"192.0.2.10"
		);

		assertThat(recordedLogin.getId()).isNotNull();
		assertThat(recordedLogin.getUser().getId()).isEqualTo(user.getId());
		assertThat(recordedLogin.getIpAddress()).isEqualTo("192.0.2.10");
		assertThat(recordedLogin.getLoginAt()).isNotNull();
		assertThat(userLoginRepo.count()).isOne();
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
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").isNotEmpty())
				.andExpect(jsonPath("$.email").value("user@example.com"))
				.andExpect(jsonPath("$.username").value("example-user"))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.passwordHash").doesNotExist());

		var storedUser = userRepo.findByEmail("user@example.com").orElseThrow();
		assertThat(storedUser.getPasswordHash()).isNotEqualTo("password123");
		assertThat(passwordEncoder.matches("password123", storedUser.getPasswordHash())).isTrue();

		var loginResult = mockMvc.perform(post("/u/login")
					.with(csrf())
					.with(request -> {
						request.setRemoteAddr("192.0.2.30");
						return request;
					})
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
		var recordedLogins = userLoginRepo.findAllByUserIdOrderByLoginAtDesc(
				storedUser.getId(),
				PageRequest.of(0, 10)
		);
		assertThat(recordedLogins).hasSize(1);
		assertThat(recordedLogins.getFirst().getIpAddress())
				.isEqualTo("192.0.2.30");

		mockMvc.perform(get("/u/me").cookie(sessionCookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("user@example.com"))
				.andExpect(jsonPath("$.username").value("example-user"))
				.andExpect(jsonPath("$.recentLogins.length()").value(1))
				.andExpect(jsonPath("$.recentLogins[0].ipAddress").value("192.0.2.30"))
				.andExpect(jsonPath("$.recentLogins[0].loginAt").isNotEmpty());

		mockMvc.perform(post("/u/logout").cookie(sessionCookie).with(csrf()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/u/me").cookie(sessionCookie))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void currentUserReturnsOnlyTenMostRecentLogins() throws Exception {
		var user = new User();
		user.setEmail("recent@example.com");
		user.setUsername("recent-user");
		user.setPasswordHash("not-used-by-this-test");
		user = userRepo.save(user);

		Instant firstLogin = Instant.parse("2026-01-01T00:00:00Z");
		for (int index = 0; index < 11; index++) {
			userLoginRepo.save(login(
					user,
					"192.0.2." + index,
					firstLogin.plusSeconds(index)
			));
		}

		mockMvc.perform(get("/u/me").with(user(UserPrincipal.from(user))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.recentLogins.length()").value(10))
				.andExpect(jsonPath("$.recentLogins[0].ipAddress").value("192.0.2.10"))
				.andExpect(jsonPath("$.recentLogins[9].ipAddress").value("192.0.2.1"));
	}

	@Test
	void registrationRejectsInvalidEmailAndShortPassword() throws Exception {
		mockMvc.perform(post("/u/register")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "email": "not-an-email",
							  "username": "example-user",
							  "password": "short",
							  "confirmPassword": "short"
							}
							"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.violations.email").isArray())
				.andExpect(jsonPath("$.violations.password[0]").value(
						"Password must be between 8 and 72 characters"
				));

		assertThat(userRepo.count()).isZero();
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

		assertThat(userLoginRepo.count()).isZero();
	}

	@Test
	void loginFailsGenericallyWithoutCreatingSessionWhenRecordingFails() throws Exception {
		mockMvc.perform(post("/u/register")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "email": "record-failure@example.com",
							  "username": "record-failure-user",
							  "password": "password123",
							  "confirmPassword": "password123"
							}
							"""))
				.andExpect(status().isOk());

		doThrow(new DataAccessResourceFailureException("database unavailable"))
				.when(userLoginService)
				.recordSuccessfulLogin(any(UserPrincipal.class), anyString());

		mockMvc.perform(post("/u/login")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "email": "record-failure@example.com",
							  "password": "password123"
							}
							"""))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.message").value("Unable to complete login"))
				.andExpect(jsonPath("$.path").value("/u/login"))
				.andExpect(cookie().doesNotExist("SESSION"));

		assertThat(userLoginRepo.count()).isZero();
	}

	@Test
	void actuatorEndpointsRequireAdminRole() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/actuator/health").with(user("regular-user").roles("USER")))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/actuator/health").with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
	}

	private static UserLogin login(User user, String ipAddress, Instant loginAt) {
		var login = new UserLogin();
		login.setUser(user);
		login.setIpAddress(ipAddress);
		login.setLoginAt(loginAt);
		return login;
	}

}
