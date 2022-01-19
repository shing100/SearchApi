package com.kingname.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kingname.api.domain.KakaoBuzz;
import com.kingname.api.repository.ElasticsearchRepository;
import com.kingname.api.common.Utils;
import com.kingname.api.vo.kakao.KakaoRequest;
import com.kingname.api.vo.kakao.KakaoResponse;
import com.kingname.api.vo.kakao.Document;
import com.kingname.api.vo.naver.Item;
import com.kingname.api.vo.naver.NaverResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
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

import static com.kingname.api.common.Utils.createIndexName;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoSearchService {

    private final ElasticsearchRepository elasticsearchRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${KEY.KAKAO}")
    private String REST_KEY;

    public void saveKakaoBuzzCount(String query, String csn) {
        String WEB = "web";
        String BLOG = "blog";
        String CAFE = "cafe";

        KakaoResponse kakaoResponse = null;
        Map<Integer, Integer> webDayCount = new LinkedHashMap<>();
        Map<Integer, Integer> blogDayCount = new LinkedHashMap<>();
        Map<Integer, Integer> cafeDayCount = new LinkedHashMap<>();

        int maxPage = 10; // 수집할 최대 페이지
        int size = 50; // 한번에 수집한 건수 :: 최대 50

        for (int i = 1; i <= maxPage; i++) {
            kakaoResponse = getKakaoApiRequest(WEB, query, i, size);
            webDayCount = getCountOfDayByDocuments(webDayCount, kakaoResponse);

            kakaoResponse = getKakaoApiRequest(BLOG, query, i, size);
            blogDayCount = getCountOfDayByDocuments(blogDayCount, kakaoResponse);

            kakaoResponse = getKakaoApiRequest(CAFE, query, i, size);
            cafeDayCount = getCountOfDayByDocuments(cafeDayCount, kakaoResponse);
        }

        List<KakaoBuzz> webBuzzList = getKakaoBuzzList(csn, WEB, query, webDayCount);
        List<KakaoBuzz> blogBuzzList = getKakaoBuzzList(csn, BLOG, query, blogDayCount);
        List<KakaoBuzz> cafeBuzzList = getKakaoBuzzList(csn, CAFE, query, cafeDayCount);

        webBuzzList.addAll(blogBuzzList);
        webBuzzList.addAll(cafeBuzzList);

        String indexName = createIndexName("kakao", Utils.getNowDateFormat("yyyyMMdd"));
        log.info("index : {} , searchWord : {}", indexName, query);
        elasticsearchRepository.bulk(indexName, webBuzzList, KakaoBuzz.class);
    }

    /**
     * 저장된 내용 CSV 파일로 저장
     * @param indexName 엘라스틱서치 인덱스명
     * @throws Exception
     */
    public void saveCsvFile(String indexName) throws Exception {
        log.info("============ SCROLL SEARCH START ============");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(50000);
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        SearchResponse searchResponse = elasticsearchRepository.searchWithScroll(indexName, searchSourceBuilder);
        String scrollId = searchResponse.getScrollId();
        SearchHit[] hits = searchResponse.getHits().getHits();

        while (hits != null && hits.length > 0) {
            saveHit(hits);
            searchResponse = elasticsearchRepository.scrollSearch(scrollId);
            scrollId = searchResponse.getScrollId();
            hits = searchResponse.getHits().getHits();
        }

        ClearScrollResponse clearScrollResponse = elasticsearchRepository.clearScroll(scrollId);
        log.info("============ SCROLL CLEAR "+ clearScrollResponse.isSucceeded() + " ============");
    }

    /**
     * 검색된 애용 저장 혹은 병합
     * @param hits
     */
    private void saveHit(SearchHit[] hits) {
        for (SearchHit hit : hits) {
            Map<String, Object> source = hit.getSourceAsMap();
        }
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
    private Map<Integer, Integer> getCountOfDayByDocuments(Map<Integer, Integer> dayCount, KakaoResponse kakaoResponse)  {
        if (kakaoResponse != null && kakaoResponse.getDocuments() != null) {
            for (Document document : kakaoResponse.getDocuments()) {
                if (document != null) {
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
        }
        return dayCount;
    }

    // 문서 날짜별 타이틀
    // 문서 날짜별 카운트와 합치는게 좋아보임
    private Map<Integer, List<String>> getTitleOfDayByDocuments(Map<Integer, List<String>> titleList, KakaoResponse kakaoResponse)  {
        if (kakaoResponse != null) {
            for (Document document : kakaoResponse.getDocuments()) {
                if (document != null) {
                    String date = Utils.getDateStrByDateTime(document.getDatetime(), "yyyy-MM-dd");
                    if (!"".equals(date)) {
                        int parseInt = Integer.parseInt(date);
                        if (!titleList.containsKey(parseInt)) {
                            titleList.put(parseInt, List.of(document.getTitle()));
                        } else {
                            List<String> list = titleList.get(parseInt);
                            list.add(document.getTitle());
                            titleList.put(parseInt, list);
                        }
                    }
                }
            }
        }
        return titleList;
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
}
