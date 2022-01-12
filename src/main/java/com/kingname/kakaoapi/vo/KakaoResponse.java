package com.kingname.kakaoapi.vo;

import com.kingname.kakaoapi.vo.kakao.Meta;
import com.kingname.kakaoapi.vo.kakao.Document;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter @Getter @ToString
public class KakaoResponse {

    private Meta meta;

    private List<Document> documents;
}
