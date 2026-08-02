package self.exercise.jobapplication.ledger.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import self.exercise.jobapplication.ledger.dto.UserSignUpLoginDTO;
import self.exercise.jobapplication.ledger.models.User;
import self.exercise.jobapplication.ledger.repositories.UserRepo;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public User findByEmail(String email) {
        return userRepo
                .findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public User saveUser(UserSignUpLoginDTO userSignUp) {
        if (userSignUp.password() == null || !userSignUp.password().equals(userSignUp.confirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password do not match");
        }
        User user = new User();
        user.setEmail(userSignUp.email());
        user.setUsername(userSignUp.username());
        user.setPasswordHash(passwordEncoder.encode(userSignUp.password()));
        return userRepo.save(user);
    }

    public User updateUser(User user) {
        return userRepo.save(user);
    }

}
