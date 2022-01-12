package com.kingname.api.vo.kakao;

import com.kingname.api.vo.kakao.Meta;
import com.kingname.api.vo.kakao.Document;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter @Getter @ToString
public class KakaoResponse {

    private Meta meta;

    private List<Document> documents;
}
