package self.exercise.jobapplication.ledger.exceptions;

public class CompanyVersionConflictException extends RuntimeException {
    public CompanyVersionConflictException() {
        super("Company has changed. Reload it before saving your changes.");
    }
}
