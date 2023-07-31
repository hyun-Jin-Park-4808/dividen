package com.example.dividend.service;

import com.example.dividend.exception.impl.NoCompanyException;
import com.example.dividend.model.Company;
import com.example.dividend.model.Dividend;
import com.example.dividend.model.ScrapedResult;
import com.example.dividend.model.constants.CacheKey;
import com.example.dividend.persist.CompanyRepository;
import com.example.dividend.persist.DividendRepository;
import com.example.dividend.persist.entity.CompanyEntity;
import com.example.dividend.persist.entity.DividendEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    // 캐싱 사용할만한 데이터인지 확인
    // 요청이 자주 들어오는가?
    // 자주 변경되는 데이터인가?
    @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
    // 캐시에 데이터가 없을 경우, 아래 로직 실행시켜서 리턴값을 캐시에 추가, 캐시에 데이터 있으면 로직 실행 안하고 캐시에 있는 데이터 반환
    public ScrapedResult getDividendByCompanyName(String companyName) {
        log.info("search company -> " + companyName);
        //1. 회사명을 기준으로 회사 정보를 조회
        CompanyEntity company = this.companyRepository.findByName(companyName)
                .orElseThrow(() -> new NoCompanyException());
        // 값이 없으면, 인자로 넘겨지는 예외 발생시키고, 값이 있으면 Opptional이 벗겨진 알맹이 뱉어냄.

        // 2. 조회된 회사 ID로 배당금 정보 조회
        List<DividendEntity> dividendEntities = this.dividendRepository.findAllByCompanyId(company.getId());

        // 3. 결과 조합 후 반환
        List<Dividend> dividends = dividendEntities.stream()
                .map(e -> new Dividend(e.getDate(), e.getDividend()))
                .collect(Collectors.toList());


        return new ScrapedResult(new Company(company.getTicker(), company.getName()),
                dividends);
    }
}
