package com.kingname.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kingname.api.common.Utils;
import com.kingname.api.domain.NaverBuzz;
import com.kingname.api.repository.ElasticsearchRepository;
import com.kingname.api.vo.naver.Item;
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

import java.util.*;
import java.util.stream.Collectors;

import static com.kingname.api.common.Utils.getNowDateFormat;
import static com.kingname.api.common.Utils.sleep;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverSearchService {

    private final ElasticsearchRepository elasticsearchRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${KEY.NAVER.ID}")
    private String NAVER_ID;

    @Value("${KEY.NAVER.SECRET}")
    private String NAVER_SECRET;

    @Value("${ELASTICSEARCH.INDEX.NAVER}")
    private String NAVER_INDEX;

    public void saveNaverBuzzCount(String query, String csn) {
        String NEWS = "news";
        String BLOG = "blog";

        NaverResponse naverResponse = null;
        Map<Integer, Integer> newsDayCount = new LinkedHashMap<>();
        Map<Integer, Integer> blogDayCount = new LinkedHashMap<>();

        int maxPage = 3; // 문서 최대 호출 페이지
        int display = 100; // 한번에 수집한 건수 :: 최대 100
        int maxDayLimit = 10;

        for (int i = 1; i < maxPage; i++) {
            int start = i > 1 ? i * display : i;
            if (newsDayCount.size() < maxDayLimit) {
                naverResponse = getNaverApiRequest(NEWS, query, start, display);
                newsDayCount = getCountOfDayByDocuments(newsDayCount, naverResponse);
                sleep(100);
            }

            if (blogDayCount.size() < maxDayLimit) {
                naverResponse = getNaverApiRequest(BLOG, query, start, display);
                blogDayCount = getCountOfDayByDocuments(blogDayCount, naverResponse);
                sleep(100);
            }
        }

        List<NaverBuzz> newsBuzzList = getNaverBuzzList(csn, NEWS, query, newsDayCount);
        List<NaverBuzz> blogBuzzList = getNaverBuzzList(csn, BLOG, query, blogDayCount);
        newsBuzzList.addAll(blogBuzzList);

        List<NaverBuzz> buzzList = newsBuzzList.stream()
                .filter(NaverBuzz::validateThisYear)    // 이번 년도 비교
                .collect(Collectors.toList());

        if (buzzList.size() > 0) {
            String today = getNowDateFormat("yyyyMMdd");
            String indexName = NAVER_INDEX.replace("yyyy", today.substring(0, 4));
            elasticsearchRepository.bulk(indexName, buzzList, NaverBuzz.class);
        }
    }

    // 날짜별 정렬
    public List<NaverBuzz> getNaverBuzzList(String csn, String type, String query, Map<Integer, Integer> dayByCount) {
        List<Map.Entry<Integer, Integer>> entries = Utils.sortMapByKey(dayByCount);
        List<NaverBuzz> kakaoBuzzList = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : entries) {
            kakaoBuzzList.add(NaverBuzz.create(csn, type, query, entry));
        }
        return kakaoBuzzList;
    }

    // 문서 날짜별 카운트
    private Map<Integer, Integer> getCountOfDayByDocuments(Map<Integer, Integer> dayCount, NaverResponse naverResponse)  {
        if (naverResponse != null) {
            for (Item item : naverResponse.getItems()) {
                if (item.getDate() != null) {
                    int parseInt = Integer.parseInt(item.getDate());
                    dayCount.merge(parseInt, 1, Integer::sum);
                }
            }
        }
        return dayCount;
    }

    // 문서 날짜별 타이틀
    // 문서 날짜별 카운트와 합치는게 좋아보임
    private Map<Integer, List<String>> getTitleOfDayByDocuments(Map<Integer, List<String>> titleList, NaverResponse naverResponse)  {
        if (naverResponse != null) {
            for (Item item : naverResponse.getItems()) {
                if (item.getDate() != null) {
                    int parseInt = Integer.parseInt(item.getDate());
                    if (!titleList.containsKey(parseInt)) {
                        titleList.put(parseInt, List.of(item.getTitle()));
                    } else {
                        List<String> list = titleList.get(parseInt);
                        list.add(item.getTitle());
                        titleList.put(parseInt, list);
                    }
                }
            }
        }
        return titleList;
    }

    /**
     * 네이버 검색 API
     * @param type // 블로그 (blog), 뉴스, 카페글, 웹문서, 지식인(?)  날짜가 있는건 뉴스, 블로그 뿐
     * @param query // 검색어
     * @return KakaoResponse.class
     */
    private NaverResponse getNaverApiRequest(String type, String query, int start, int display) {
        try {
            log.debug("ID : {} , SECRET : {}", NAVER_ID, NAVER_SECRET);

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

            return restTemplate.exchange(components.toUriString(), HttpMethod.GET,
                    new HttpEntity<>(httpHeaders), NaverResponse.class)
                    .getBody();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new NaverResponse();
    }
}
