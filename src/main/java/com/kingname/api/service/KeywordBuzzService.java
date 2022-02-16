package com.kingname.api.service;

import com.kingname.api.repository.ElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordBuzzService {

    private final ElasticsearchRepository elasticsearchRepository;

    public Map<String, Double> getCompanyBuzzHistogram(String csn, String to, String from) throws IOException {
        String aggName = "buzz_chart";

        SearchSourceBuilder searchSourceBuilder = getCsnAndDateRangeQuery(csn, aggName, from, to);
        SearchResponse searchResponse = elasticsearchRepository.search("keyword_analysis_*-202202", searchSourceBuilder);
        Histogram histogram = searchResponse.getAggregations().get(aggName);
        Map<String, Double> result = new HashMap<>();

        for (Histogram.Bucket bucket : histogram.getBuckets()) {
            String week = bucket.getKeyAsString();
            Sum countAggs = bucket.getAggregations().get("count");
            double value = countAggs.getValue();
            log.info("week {} , count {}", week, value);
            // TODO value 값을 가지고 와서 5주차 값을 -> 수치로 변경
            result.put(week, value);
        }
        return result;
    }

    private SearchSourceBuilder getCsnAndDateRangeQuery(String searchWord, String aggName, String from, String to) {
        String rangeField = "event_date";
        String sumField = "count";
        int intervalDay = 7;

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("searchWord", searchWord))
                .must(QueryBuilders.rangeQuery(rangeField).from(from).to(to));

        DateHistogramAggregationBuilder aggregations = AggregationBuilders.dateHistogram(aggName)
                .field(rangeField)
                .fixedInterval(DateHistogramInterval.days(intervalDay))
                .subAggregation(AggregationBuilders.sum(sumField).field(sumField));

        return SearchSourceBuilder.searchSource().query(queryBuilder).aggregation(aggregations).size(0);
    }
}
