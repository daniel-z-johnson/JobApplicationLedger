package self.exercise.jobapplication.ledger.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import self.exercise.jobapplication.ledger.dto.UserRegistrationRequest;
import self.exercise.jobapplication.ledger.exceptions.DuplicateUserException;
import self.exercise.jobapplication.ledger.models.User;
import self.exercise.jobapplication.ledger.repositories.UserRepo;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public User findByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        return userRepo
                .findByEmail(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + normalizedEmail));
    }

    @Transactional
    public User saveUser(UserRegistrationRequest registrationRequest) {
        String normalizedEmail = normalizeEmail(registrationRequest.email());

        if (userRepo.existsByEmail(normalizedEmail)) {
            throw new DuplicateUserException();
        }
        if (userRepo.existsByUsername(registrationRequest.username())) {
            throw new DuplicateUserException();
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setUsername(registrationRequest.username());
        user.setPasswordHash(passwordEncoder.encode(registrationRequest.password()));
        return userRepo.save(user);
    }

    public User updateUser(User user) {
        return userRepo.save(user);
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
