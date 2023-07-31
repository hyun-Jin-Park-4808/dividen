package com.example.dividend.scraper;

import com.example.dividend.model.Company;
import com.example.dividend.model.Dividend;
import com.example.dividend.model.ScrapedResult;
import com.example.dividend.model.constants.Month;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;

@Component
public class YahooFinanceScraper implements Scraper {

    private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";
    // ticker 들어갈 자리: %s, 배당조회 시작날짜: period1=%d, 배당조회 끝 날짜: period2=%d
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s"; // 티커로 회사 이름 조회하기 위한 url

    private static final long START_TIME = 86400; // 시작 날짜 60 sec * 60 min * 24 hour

    @Override
    public ScrapedResult scrap(Company company) {
        var scrapResult = new ScrapedResult();
        scrapResult.setCompany(company);

        try {
            long now = System.currentTimeMillis() / 1000; // 밀리세컨 단위를 초 단위로 바꿈

            String url = String.format(STATISTICS_URL, company.getTicker(), START_TIME, now);
            Connection connection = Jsoup.connect(url);
            Document document = connection.get(); // 연결 객체에서 get 형식으로 문서 받아옴.

            Elements parsingDivs = document.getElementsByAttributeValue("data-test", "historical-prices");
            Element tableElement = parsingDivs.get(0); // 테이블 객체

            Element tbody = tableElement.children().get(1);

            List<Dividend> dividends = new ArrayList<>(); // 배당금 정보 담을 리스트
            for (Element e : tbody.children()) { // tbody 안에 모든 데이터 순회
                String txt = e.text();
                if (!txt.endsWith("Dividend")) {
                    continue;
                }

                String[] splits = txt.split(" "); // 공백 기준으로 쪼개줌.
                int month = Month.strToNumber(splits[0]);
                int day = Integer.valueOf(splits[1].replace(",", "")); // 1 번째 인덱스에서 ,삭제하면 일
                int year = Integer.valueOf(splits[2]); // 2 번째 인덱스는 년
                String dividend = splits[3]; // 3 번재 인덱스는 배당금

                if (month < 0) {
                    throw new RuntimeException("Unexpected Month enum value -> " + splits[0]);
                }

                dividends.add(new Dividend(LocalDateTime.of(year, month, day, 0, 0), dividend));
            }
            scrapResult.setDividends(dividends);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return scrapResult;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker) {
        String url = String.format(SUMMARY_URL, ticker, ticker);

        try {
            Document document = Jsoup.connect(url).get();
            Element titleElement = document.getElementsByTag("h1").get(0);
            String title = titleElement.text().split(" - ")[1].trim();
            // abc - def - hij => abc, def, hij를 반환하고 그중 1번째 인덱스 값인 def를 앞 뒤 공백 제거(trim)해서 반환

            return new Company(ticker, title);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
