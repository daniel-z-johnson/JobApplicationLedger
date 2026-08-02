package self.exercise.jobapplication.ledger.controllers;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import self.exercise.jobapplication.ledger.dto.UserDTO;
import self.exercise.jobapplication.ledger.dto.UserSignUpLoginDTO;
import self.exercise.jobapplication.ledger.models.User;
import self.exercise.jobapplication.ledger.services.UserService;

@RestController
@RequestMapping("/u")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public UserDTO signup(@Valid @RequestBody UserSignUpLoginDTO userSignUpLoginDTO) {
        User savedUser = userService.saveUser(userSignUpLoginDTO);
        return new UserDTO(savedUser.getEmail(), savedUser.getUsername(), savedUser.getCreatedAt(), savedUser.getUpdatedAt());
    }
}
