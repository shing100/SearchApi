package com.kingname.api.vo.naver;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter @Builder
public class NaverRequest {

    private String query;
    private final String sort = "date"; // 날짜순 : date , 유사도순 : sim
    private int start;
    private int display;
}
