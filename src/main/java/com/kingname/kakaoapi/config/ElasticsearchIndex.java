package com.kingname.kakaoapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter @Setter
public class ElasticsearchIndex {

    private String indexName;

}
