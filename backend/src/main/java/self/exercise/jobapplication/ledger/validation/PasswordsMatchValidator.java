package self.exercise.jobapplication.ledger.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import self.exercise.jobapplication.ledger.dto.UserRegistrationRequest;

import java.util.Objects;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, UserRegistrationRequest> {

    @Override
    public boolean isValid(UserRegistrationRequest request, ConstraintValidatorContext context) {
        return request == null || Objects.equals(request.password(), request.confirmPassword());
    }
}
