package self.exercise.jobapplication.ledger.services;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import self.exercise.jobapplication.ledger.dto.CompanyRequest;
import self.exercise.jobapplication.ledger.dto.CompanyResponse;
import self.exercise.jobapplication.ledger.exceptions.CompanyNotFoundException;
import self.exercise.jobapplication.ledger.exceptions.CompanyVersionConflictException;
import self.exercise.jobapplication.ledger.models.Company;
import self.exercise.jobapplication.ledger.repositories.CompanyRepo;
import self.exercise.jobapplication.ledger.repositories.UserRepo;

import java.util.UUID;

/** Callers must obtain userId from the authenticated principal, never from request data. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {
    private final CompanyRepo companyRepo;
    private final UserRepo userRepo;
    private final Validator validator;

    public Page<CompanyResponse> findAllByUserId(UUID userId, Pageable pageable) {
        Assert.notNull(userId, "User ID is required");
        Assert.notNull(pageable, "Pagination is required");
        Assert.isTrue(pageable.isPaged(), "Pagination is required");
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by("name");
        if (sort.getOrderFor("id") == null) {
            sort = sort.and(Sort.by("id"));
        }
        return companyRepo.findAllByUserId(userId,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort))
                .map(CompanyResponse::from);
    }

    public CompanyResponse findByIdAndUserId(UUID companyId, UUID userId) {
        return CompanyResponse.from(requireOwnedCompany(companyId, userId));
    }

    @Transactional
    public CompanyResponse createCompany(UUID userId, CompanyRequest request) {
        Assert.notNull(userId, "User ID is required");
        validate(request);
        Assert.isNull(request.version(), "Version must be omitted when creating a company");
        var owner = userRepo.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Company company = new Company();
        company.setUser(owner);
        apply(company, request);
        Company saved = companyRepo.saveAndFlush(company);
        return CompanyResponse.from(saved);
    }

    /** Replaces editable fields; omitted optional URLs are cleared. */
    @Transactional
    public CompanyResponse updateCompany(UUID companyId, UUID userId, CompanyRequest request) {
        Company company = requireOwnedCompany(companyId, userId);
        validate(request);
        Assert.notNull(request.version(), "Version is required when updating a company");
        if (!company.getVersion().equals(request.version())) {
            throw new CompanyVersionConflictException();
        }
        apply(company, request);
        Company saved = companyRepo.saveAndFlush(company);
        return CompanyResponse.from(saved);
    }

    @Transactional
    public void deleteCompany(UUID companyId, UUID userId) {
        companyRepo.delete(requireOwnedCompany(companyId, userId));
    }

    private Company requireOwnedCompany(UUID companyId, UUID userId) {
        Assert.notNull(companyId, "Company ID is required");
        Assert.notNull(userId, "User ID is required");
        return companyRepo.findByIdAndUserId(companyId, userId)
                .orElseThrow(CompanyNotFoundException::new);
    }

    private void validate(CompanyRequest request) {
        Assert.notNull(request, "Company details are required");
        Assert.isTrue(validator.validate(request).isEmpty(), "Invalid company details");
    }

    private static void apply(Company company, CompanyRequest request) {
        company.setName(request.name());
        company.setCompanyType(request.companyType());
        company.setWebsiteUrl(request.websiteUrl());
        company.setCareersUrl(request.careersUrl());
    }
}
