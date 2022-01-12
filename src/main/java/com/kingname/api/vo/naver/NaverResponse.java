package com.kingname.api.vo.naver;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Setter @Getter @ToString
public class NaverResponse {

    private List<Map<String, Object>> documents = new ArrayList<>();
}
