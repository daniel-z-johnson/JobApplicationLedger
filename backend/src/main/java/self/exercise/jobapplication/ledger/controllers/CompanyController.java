package self.exercise.jobapplication.ledger.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import self.exercise.jobapplication.ledger.dto.CompanyRequest;
import self.exercise.jobapplication.ledger.dto.CompanyResponse;
import self.exercise.jobapplication.ledger.security.UserPrincipal;
import self.exercise.jobapplication.ledger.services.CompanyService;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> SORT_FIELDS = Set.of("id", "name", "companyType", "createdAt", "updatedAt");

    private final CompanyService companyService;

    @GetMapping
    public PagedModel<CompanyResponse> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        for (var order : pageable.getSort()) {
            if (!SORT_FIELDS.contains(order.getProperty())) {
                throw new IllegalArgumentException("Unsupported company sort field");
            }
        }
        var boundedPage = PageRequest.of(pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), MAX_PAGE_SIZE), pageable.getSort());
        return new PagedModel<>(companyService.findAllByUserId(principal.getId(), boundedPage));
    }

    @GetMapping("/{companyId}")
    public CompanyResponse get(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID companyId) {
        return companyService.findByIdAndUserId(companyId, principal.getId());
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CompanyRequest request) {
        var company = companyService.createCompany(principal.getId(), request);
        var location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{companyId}").buildAndExpand(company.id()).toUri();
        return ResponseEntity.created(location).body(company);
    }

    @PutMapping("/{companyId}")
    public CompanyResponse update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID companyId,
            @Valid @RequestBody CompanyRequest request) {
        return companyService.updateCompany(companyId, principal.getId(), request);
    }

    @DeleteMapping("/{companyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID companyId) {
        companyService.deleteCompany(companyId, principal.getId());
    }
}
