package com.example.dividend.service;

import com.example.dividend.exception.impl.NoCompanyException;
import com.example.dividend.model.Company;
import com.example.dividend.model.ScrapedResult;
import com.example.dividend.persist.CompanyRepository;
import com.example.dividend.persist.DividendRepository;
import com.example.dividend.persist.entity.CompanyEntity;
import com.example.dividend.persist.entity.DividendEntity;
import com.example.dividend.scraper.Scraper;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.stream.Collectors;
import java.util.List;

@Service
@AllArgsConstructor
public class CompanyService {

    private final Trie trie; // Appconfig에서 Trie Bean이 초기화되고 CompanyService에 주입이 되면서 CompanyService 의 인스턴스로 사용 가능
    private final Scraper yahooFinanceScraper;

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public Company save(String ticker) {
        boolean exists = this.companyRepository.existsByTicker(ticker); // 조회한 회사가 이미 DB에 존재하는지 결과 받음
        if (exists) {
            throw new RuntimeException("already exists ticker -> " + ticker);
        }
        return this.storeCompanyAndDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable) {
        return this.companyRepository.findAll(pageable);
    }

    private Company storeCompanyAndDividend(String ticker) {
        // ticker 를 기준으로 회사를 스크래핑
        Company company = this.yahooFinanceScraper.scrapCompanyByTicker(ticker);
        if (ObjectUtils.isEmpty(company)) {
            throw new RuntimeException("failed to scrap ticker -> " + ticker);
        }

        // 해당 회사가 존재할 경우, 회사의 배당금 정보를 스크래핑
        ScrapedResult scrapedResult= this.yahooFinanceScraper.scrap(company);

        // 스크래핑 결과
        CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company)); // id값도 저장하기 위해 company를 CompanyEntity 객체로 변환해서 저장
        List<DividendEntity> dividendEntityList = scrapedResult.getDividends().stream()
                .map(e -> new DividendEntity(companyEntity.getId(), e)) // dividend 하나 하나가 e에 매핑됨.
                .collect(Collectors.toList()); // 결과값을 리스트 타입으로 반환

        this.dividendRepository.saveAll(dividendEntityList);
        return company;
    }

    public List<String> getCompanyNamedByKeyword(String keyword) {
        Pageable limit = PageRequest.of(0, 10);
        Page<CompanyEntity> companyEntities = this.companyRepository.findByNameStartingWithIgnoreCase(keyword, limit);
        return companyEntities.stream()
                .map(e -> e.getName())
                .collect(Collectors.toList());
    }

    public void addAutocompleteKeyword(String keyword) {
        this.trie.put(keyword, null); // 회사명 저장
    }

    public List<String> autocomplete(String keyword) { // 트라이에서 단어 찾아오는 메서드
        return (List<String>) this.trie.prefixMap(keyword).keySet()
                .stream()
                .collect(Collectors.toList());
    }

    public void deleteAutocompleteKeyword(String keyword) { // 자동완성 단어 지우는 메서드
        this.trie.remove(keyword);
    }

    public String deleteCompany(String ticker) {
        var company = this.companyRepository.findByTicker(ticker)
                .orElseThrow(() -> new NoCompanyException());

        this.dividendRepository.deleteAllByCompanyId(company.getId());
        this.companyRepository.delete(company);

        this.deleteAutocompleteKeyword(company.getName()); // 자동완성된 회사 이름도 삭제

        return company.getName();
    }

}
