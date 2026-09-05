package self.exercise.jobapplication.ledger.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import self.exercise.jobapplication.ledger.security.UserPrincipal;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserService userService;

    @Override
    public UserPrincipal loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = userService.findByEmail(email);
        return UserPrincipal.from(user);
    }
}
