package self.exercise.jobapplication.ledger.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import self.exercise.jobapplication.ledger.dto.UserDTO;
import self.exercise.jobapplication.ledger.dto.UserSignUpLoginDTO;
import self.exercise.jobapplication.ledger.models.User;
import self.exercise.jobapplication.ledger.services.UserDetailsServiceImpl;
import self.exercise.jobapplication.ledger.services.UserService;

@RequestMapping("/u")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder encoder;

    @PostMapping("/register")
    public UserDTO signup(UserSignUpLoginDTO userSignUpLoginDTO) {
        User savedUser = userService.saveUser(userSignUpLoginDTO);
        return new UserDTO(savedUser.getEmail(), savedUser.getUsername(), savedUser.getCreatedAt(), savedUser.getUpdatedAt());
    }
}
