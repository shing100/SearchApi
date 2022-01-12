package com.kingname.api.vo.naver;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Setter @Getter @ToString
public class NaverResponse {

    private int total;
    private int start;
    private int display;

    private List<Item> items = new ArrayList<>();
}
