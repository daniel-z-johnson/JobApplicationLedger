package self.exercise.jobapplication.ledger.dto;

import self.exercise.jobapplication.ledger.models.Company;

import java.time.Instant;
import java.util.UUID;

public record CompanyResponse(UUID id, String name, String companyType,
                              String websiteUrl, String careersUrl,
                              Instant createdAt, Instant updatedAt) {
    public static CompanyResponse from(Company company) {
        return new CompanyResponse(company.getId(), company.getName(), company.getCompanyType(),
                company.getWebsiteUrl(), company.getCareersUrl(),
                company.getCreatedAt(), company.getUpdatedAt());
    }
}
