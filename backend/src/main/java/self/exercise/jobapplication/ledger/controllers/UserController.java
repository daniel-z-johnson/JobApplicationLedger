package self.exercise.jobapplication.ledger.controllers;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfLogoutHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import self.exercise.jobapplication.ledger.dto.CurrentUserResponse;
import self.exercise.jobapplication.ledger.dto.UserLoginRequest;
import self.exercise.jobapplication.ledger.dto.UserRegistrationRequest;
import self.exercise.jobapplication.ledger.dto.UserResponse;
import self.exercise.jobapplication.ledger.exceptions.LoginRecordingException;
import self.exercise.jobapplication.ledger.models.User;
import self.exercise.jobapplication.ledger.security.UserPrincipal;
import self.exercise.jobapplication.ledger.services.UserLoginService;
import self.exercise.jobapplication.ledger.services.UserService;
import self.exercise.jobapplication.ledger.web.ClientIpAddressResolver;


@RestController
@RequestMapping("/u")
@RequiredArgsConstructor
public class UserController {
    private static final int RECENT_LOGIN_LIMIT = 10;

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final CsrfTokenRepository csrfTokenRepository;
    private final UserLoginService userLoginService;
    private final ClientIpAddressResolver clientIpAddressResolver;

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }

    @PostMapping("/register")
    public UserResponse signup(@Valid @RequestBody UserRegistrationRequest registrationRequest) {
        User savedUser = userService.saveUser(registrationRequest);
        return UserResponse.from(savedUser);
    }

    @PostMapping("/login")
    public UserResponse login(
            @Valid @RequestBody UserLoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );
        if (authentication instanceof CredentialsContainer credentialsContainer) {
            credentialsContainer.eraseCredentials();
        }

        var principal = (UserPrincipal) authentication.getPrincipal();
        try {
            userLoginService.recordSuccessfulLogin(
                    principal,
                    clientIpAddressResolver.resolve(request)
            );
        } catch (org.springframework.security.core.AuthenticationException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new LoginRecordingException(exception);
        }

        sessionAuthenticationStrategy.onAuthentication(authentication, request, response);

        var securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        securityContextRepository.saveContext(securityContext, request, response);

        return UserResponse.from(principal);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response) {
        new CsrfLogoutHandler(csrfTokenRepository).logout(request, response, authentication);
        new SecurityContextLogoutHandler().logout(request, response, authentication);
    }

    @GetMapping("/me")
    public CurrentUserResponse currentUser(@AuthenticationPrincipal UserPrincipal principal) {
        var recentLogins = userLoginService.findRecentLogins(
                principal.getId(),
                RECENT_LOGIN_LIMIT
        );
        return CurrentUserResponse.from(principal, recentLogins);
    }

}
