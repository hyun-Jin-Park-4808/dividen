package com.example.dividend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor // 모든 필드 초기화하는 생성자 코드 사용 가능
public class ScrapedResult { // 스크래핑한 결과 주고받기 위한 클래스

    private Company company; // 스크래핑한 회사 이름

    private List<Dividend> dividends; // 그 회사의 배당금 정보들

    public ScrapedResult() {
        this.dividends = new ArrayList<>();
    }
}
