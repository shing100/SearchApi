package com.kingname.api.service;

import com.kingname.api.common.Utils;
import com.kingname.api.repository.ElasticsearchRepository;
import com.kingname.api.vo.Buzz;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordBuzzService {

    private final ElasticsearchRepository elasticsearchRepository;

    public List<Buzz> getCompanyBuzzHistogram(String csn, String type) throws IOException {
        List<Buzz> result = new ArrayList<>();
        String aggName = "buzz_chart";

        LocalDate to = LocalDate.now().minusDays(8);
        LocalDate from = to.minusWeeks(6);

        if ("m".equals(type)) {
            from = to.minusWeeks(4);
        }

        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyyMMdd");
        SearchSourceBuilder searchSourceBuilder = getCsnAndDateRangeQuery(csn, aggName, from.format(pattern), to.format(pattern));
        SearchResponse searchResponse = elasticsearchRepository.search("keyword_analysis_*", searchSourceBuilder);
        Histogram histogram = searchResponse.getAggregations().get(aggName);

        double denominator = 0;
        for (Histogram.Bucket bucket : histogram.getBuckets()) {
            Sum countAggs = bucket.getAggregations().get("count");
            denominator += countAggs.getValue();
        }

        for (Histogram.Bucket bucket : histogram.getBuckets()) {
            Sum countAggs = bucket.getAggregations().get("count");
            Buzz buzz = new Buzz(bucket.getKeyAsString(), (double) Math.round((countAggs.getValue() / denominator) * 100));
            result.add(buzz);
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
                .subAggregation(AggregationBuilders.sum(sumField).field(sumField))
                .order(BucketOrder.key(false));

        return SearchSourceBuilder.searchSource().query(queryBuilder).aggregation(aggregations).size(0);
    }
}
