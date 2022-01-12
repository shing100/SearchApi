package com.kingname.kakaoapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kingname.kakaoapi.domain.KakaoBuzz;
import com.kingname.kakaoapi.repository.ElasticsearchRepository;
import com.kingname.kakaoapi.utils.Utils;
import com.kingname.kakaoapi.vo.KakaoRequest;
import com.kingname.kakaoapi.vo.KakaoResponse;
import com.kingname.kakaoapi.vo.kakao.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoSearchService {

    private final ElasticsearchRepository elasticsearchRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("KAKAO.KEY.REST")
    private final String REST_KEY;

    public void saveKakaoBuzzCount(String query, String csn) {
        String WEB = "web";
        String BLOG = "blog";
        String CAFE = "cafe";

        KakaoResponse kakaoResponse = null;
        Map<Integer, Integer> webDayCount = new LinkedHashMap<>();
        Map<Integer, Integer> blogDayCount = new LinkedHashMap<>();
        Map<Integer, Integer> cafeDayCount = new LinkedHashMap<>();

        int maxPage = 5; // 수집할 최대 페이지
        int size = 50; // 한번에 수집한 건수 :: 최대 50

        for (int i = 1; i <= maxPage; i++) {
            kakaoResponse = getKakaoApiRequest(WEB, query, i, size);
            webDayCount = getDocumentsOfDayByCount(webDayCount, kakaoResponse);

            kakaoResponse = getKakaoApiRequest(BLOG, query, i, size);
            blogDayCount = getDocumentsOfDayByCount(blogDayCount, kakaoResponse);

            kakaoResponse = getKakaoApiRequest(CAFE, query, i, size);
            cafeDayCount = getDocumentsOfDayByCount(cafeDayCount, kakaoResponse);
        }

        List<KakaoBuzz> webBuzzList = getKakaoBuzzList(csn, WEB, query, webDayCount);
        List<KakaoBuzz> blogBuzzList = getKakaoBuzzList(csn, BLOG, query, blogDayCount);
        List<KakaoBuzz> cafeBuzzList = getKakaoBuzzList(csn, CAFE, query, cafeDayCount);

        webBuzzList.addAll(blogBuzzList);
        webBuzzList.addAll(cafeBuzzList);

        String today = Utils.getNowDateFormat("yyyyMMdd");
        String indexName = createIndexName(today);
        log.info(indexName);
        elasticsearchRepository.bulk(indexName, webBuzzList, KakaoBuzz.class);
    }

    // 날짜별 정렬
    public List<KakaoBuzz> getKakaoBuzzList(String csn, String type, String query, Map<Integer, Integer> dayByCount) {
        List<Map.Entry<Integer, Integer>> entries = Utils.sortMapByKey(dayByCount);
        List<KakaoBuzz> kakaoBuzzList = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : entries) {
            kakaoBuzzList.add(KakaoBuzz.create(csn, type, query, entry));
        }
        return kakaoBuzzList;
    }

    // 문서 날짜별 카운트
    private Map<Integer, Integer> getDocumentsOfDayByCount(Map<Integer, Integer> dayCount, KakaoResponse kakaoResponse)  {
        if (kakaoResponse != null) {
            for (Document document : kakaoResponse.getDocuments()) {
                String date = Utils.getDateStrByDateTime(document.getDatetime(), "yyyy-MM-dd");
                if (!"".equals(date)) {
                    int parseInt = Integer.parseInt(date);
                    if (!dayCount.containsKey(parseInt)) {
                        dayCount.put(parseInt, 1);
                    } else {
                        dayCount.put(parseInt, dayCount.get(parseInt) + 1);
                    }
                }
            }
        }
        return dayCount;
    }


    /**
     * 카카오 검색 API
     * @param type // WEB, BLOG, CAFE 그외도 동영상 등...
     * @param query // 검색어
     * @return KakaoResponse.class
     */
    private KakaoResponse getKakaoApiRequest(String type, String query, int page, int size) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(HttpHeaders.AUTHORIZATION, "KakaoAK " + REST_KEY);
            KakaoRequest requestVO = KakaoRequest.builder().query(query).page(page).size(size).build();
            MultiValueMap<String, String> multiValueMap = Utils.convertRequestVO(objectMapper, requestVO);

            UriComponents components = UriComponentsBuilder.newInstance()
                    .scheme("https").host("dapi.kakao.com").path("/v2/search/").path(type)
                    .queryParams(multiValueMap)
                    .build();

            return restTemplate.exchange(components.toUriString(), HttpMethod.GET,
                            new HttpEntity<>(httpHeaders), KakaoResponse.class)
                    .getBody();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new KakaoResponse();
    }

    private String createIndexName(String date) {
        String IndexPattern = "buzz-kakao-YYYYMM";
        return IndexPattern.replace("YYYYMM", date.substring(0,6));
    }
}
