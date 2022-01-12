package com.kingname.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kingname.api.common.Utils;
import com.kingname.api.domain.NaverBuzz;
import com.kingname.api.repository.ElasticsearchRepository;
import com.kingname.api.vo.naver.NaverRequest;
import com.kingname.api.vo.naver.NaverResponse;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverSearchService {

    private final ElasticsearchRepository elasticsearchRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("KEY.NAVER.ID")
    private String NAVER_ID;

    @Value("KEY.NVAER.SECRET")
    private String NAVER_SECRET;


    public void saveNaverBuzzCount(String query, String csn) {
        String NEWS = "news";
        String BLOG = "blog";
        String CAFE = "cafearticle";

        NaverResponse naverResponse = null;
        Map<Integer, Integer> newsDayCount = new LinkedHashMap<>();
        Map<Integer, Integer> blogDayCount = new LinkedHashMap<>();
        Map<Integer, Integer> cafeDayCount = new LinkedHashMap<>();

        int maxPage = 5; // 문서 최대 호출 페이지
        int display = 100; // 한번에 수집한 건수 :: 최대 100

        for (int i = 0; i < maxPage; i++) {
            naverResponse = getNaverApiRequest(NEWS, query, i * display, display);
            newsDayCount = getDocumentsOfDayByCount(newsDayCount, naverResponse);

            naverResponse = getNaverApiRequest(BLOG, query, i * display, display);
            blogDayCount = getDocumentsOfDayByCount(blogDayCount, naverResponse);

            naverResponse = getNaverApiRequest(CAFE, query, i * display, display);
            cafeDayCount = getDocumentsOfDayByCount(cafeDayCount, naverResponse);
        }

        List<NaverBuzz> newsBuzzList = getKakaoBuzzList(csn, NEWS, query, newsDayCount);
        List<NaverBuzz> blogBuzzList = getKakaoBuzzList(csn, BLOG, query, blogDayCount);
        List<NaverBuzz> cafeBuzzList = getKakaoBuzzList(csn, CAFE, query, cafeDayCount);

        newsBuzzList.addAll(blogBuzzList);
        newsBuzzList.addAll(cafeBuzzList);

        String indexName = Utils.createIndexName("naver", Utils.getNowDateFormat("yyyyMMdd"));
        log.info("index : {} , searchWord : {}", indexName, query);
        elasticsearchRepository.bulk(indexName, newsBuzzList, NaverBuzz.class);
    }

    // 날짜별 정렬
    public List<NaverBuzz> getKakaoBuzzList(String csn, String type, String query, Map<Integer, Integer> dayByCount) {
        List<Map.Entry<Integer, Integer>> entries = Utils.sortMapByKey(dayByCount);
        List<NaverBuzz> kakaoBuzzList = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : entries) {
            kakaoBuzzList.add(NaverBuzz.create(csn, type, query, entry));
        }
        return kakaoBuzzList;
    }

    // 문서 날짜별 카운트
    private Map<Integer, Integer> getDocumentsOfDayByCount(Map<Integer, Integer> dayCount, NaverResponse naverResponse)  {
        if (naverResponse != null) {
//            for (Document document : naverResponse.getDocuments()) {
//                String date = Utils.getDateStrByDateTime(document.getDatetime(), "yyyy-MM-dd");
//                if (!"".equals(date)) {
//                    int parseInt = Integer.parseInt(date);
//                    if (!dayCount.containsKey(parseInt)) {
//                        dayCount.put(parseInt, 1);
//                    } else {
//                        dayCount.put(parseInt, dayCount.get(parseInt) + 1);
//                    }
//                }
//            }
        }
        return dayCount;
    }

    /**
     * 네이버 검색 API
     * @param type // 블로그 (blog), 뉴스, 카페글, 웹문서, 지식인(?)
     * @param query // 검색어
     * @return KakaoResponse.class
     */
    private NaverResponse getNaverApiRequest(String type, String query, int start, int display) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-Naver-Client-Id", NAVER_ID);
            httpHeaders.add("X-Naver-Client-Secret", NAVER_SECRET);
            NaverRequest requestVO = NaverRequest.builder().query(query).start(start).display(display).build();
            MultiValueMap<String, String> multiValueMap = Utils.convertRequestVO(objectMapper, requestVO);

            String jsonType = ".json";
            UriComponents components = UriComponentsBuilder.newInstance()
                    .scheme("https").host("openapi.naver.com").path("/v1/search/").path(type + jsonType)
                    .queryParams(multiValueMap)
                    .build();

            List list = restTemplate.exchange(components.toUriString(), HttpMethod.GET, new HttpEntity<>(httpHeaders), List.class).getBody();
            for (Object object : list) {
                log.info(object.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new NaverResponse();
    }
}
