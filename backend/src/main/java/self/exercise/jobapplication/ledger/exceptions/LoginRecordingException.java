package self.exercise.jobapplication.ledger.exceptions;

public class LoginRecordingException extends RuntimeException {
    public LoginRecordingException(Throwable cause) {
        super("Unable to record successful login", cause);
    }
}
