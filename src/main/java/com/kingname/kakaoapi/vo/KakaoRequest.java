package com.kingname.kakaoapi.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter @Builder
public class KakaoRequest {

    private String query;
    private String sort = "recency"; // 최신순 : recency , 정확도순 : accuracy
    private int page;
    private int size;
}
