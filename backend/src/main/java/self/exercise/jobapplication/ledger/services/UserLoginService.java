package self.exercise.jobapplication.ledger.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import self.exercise.jobapplication.ledger.models.UserLogin;
import self.exercise.jobapplication.ledger.repositories.UserLoginRepo;
import self.exercise.jobapplication.ledger.repositories.UserRepo;
import self.exercise.jobapplication.ledger.security.UserPrincipal;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserLoginService {
    private final UserLoginRepo userLoginRepo;
    private final UserRepo userRepo;

    @Transactional
    public UserLogin recordSuccessfulLogin(UserPrincipal principal, String ipAddress) {
        var user = userRepo.findById(principal.getId())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with id: " + principal.getId()
                ));

        var login = new UserLogin();
        login.setUser(user);
        login.setIpAddress(ipAddress);
        return userLoginRepo.save(login);
    }

    @Transactional(readOnly = true)
    public List<UserLogin> findRecentLogins(UUID userId, int limit) {
        return userLoginRepo.findAllByUserIdOrderByLoginAtDesc(
                userId,
                PageRequest.of(0, limit)
        );
    }
}
