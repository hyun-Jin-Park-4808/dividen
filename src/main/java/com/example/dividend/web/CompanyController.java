package com.example.dividend.web;

import com.example.dividend.model.constants.CacheKey;
import com.example.dividend.service.CompanyService;
import com.example.dividend.model.Company;
import com.example.dividend.persist.entity.CompanyEntity;
import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/company") // 각 API 경로에 공통되는 부분 여기에 써주면 각각 공통 경로는 안써줘도 됨.
@AllArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    private final CacheManager redisCacheManager;

    @GetMapping("/autocomplete") // 회사이름 자동 완성 API
    public ResponseEntity<?> autocomplete(@RequestParam String keyword) {
        var result = this.companyService.getCompanyNamedByKeyword(keyword);
        return ResponseEntity.ok(result);
    }

    @GetMapping // 회사 이름 조회 API
    @PreAuthorize("hasRole('READ')") // read 권한이 있는 사용자만접근 가능
    public ResponseEntity<?> searchCompany(final Pageable pageable) {
        Page<CompanyEntity> companies = this.companyService.getAllCompany(pageable);
        return ResponseEntity.ok(companies);
    }

    /**
     * 회사 및 배당금 정보 추가
     * @param request
     * @return
     */
    @PostMapping // 회사 저장 API
    @PreAuthorize("hasRole('WRITE')")
    public ResponseEntity<?> addCompany(@RequestBody Company request) { // body에 입력되는 대용이 request로 들어감
        String ticker = request.getTicker().trim();

        if (ObjectUtils.isEmpty(ticker)) { // ticker 값이 null 인 경우
            throw new RuntimeException("ticker is empty");
        }

        Company company = this.companyService.save(ticker); // 저장한 결과 나옴.
        this.companyService.addAutocompleteKeyword(company.getName());
        return ResponseEntity.ok(company); // 회사 정보 반환
    }

    @DeleteMapping("/{ticker}") // 회사 삭제 API
    @PreAuthorize("hasRole('WRITE')")
    public ResponseEntity<?> deleteCompany(@PathVariable String ticker) {
        String companyName = this.companyService.deleteCompany(ticker); // 엔티티 삭제
        this.clearFinanceCache(companyName); // 캐쉬 삭제
        return ResponseEntity.ok(companyName); // 삭제된 회사명 반환
    }

    public void clearFinanceCache(String companyName) {
        this.redisCacheManager.getCache(CacheKey.KEY_FINANCE).evict(companyName);

    }

}
