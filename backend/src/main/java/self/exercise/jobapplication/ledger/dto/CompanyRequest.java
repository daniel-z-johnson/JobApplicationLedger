package self.exercise.jobapplication.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.validator.constraints.URL;

public record CompanyRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 127) String companyType,
        @URL @Pattern(regexp = "(?i)^https?://.*", message = "Must use HTTP or HTTPS") String websiteUrl,
        @URL @Pattern(regexp = "(?i)^https?://.*", message = "Must use HTTP or HTTPS") String careersUrl,
        @PositiveOrZero Long version) {
    public CompanyRequest(String name, String companyType, String websiteUrl, String careersUrl) {
        this(name, companyType, websiteUrl, careersUrl, null);
    }

    public CompanyRequest {
        name = name == null ? null : name.strip();
        companyType = companyType == null ? null : companyType.strip();
        websiteUrl = normalizeOptional(websiteUrl);
        careersUrl = normalizeOptional(careersUrl);
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
