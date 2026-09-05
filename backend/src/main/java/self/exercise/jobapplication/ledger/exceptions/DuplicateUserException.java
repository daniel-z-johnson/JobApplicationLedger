package self.exercise.jobapplication.ledger.exceptions;

public class DuplicateUserException extends RuntimeException {

    public DuplicateUserException() {
        super("An account with those details already exists");
    }
}
