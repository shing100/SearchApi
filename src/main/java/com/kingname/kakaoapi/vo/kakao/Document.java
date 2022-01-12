package com.kingname.kakaoapi.vo.kakao;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
public class Document {

    // 웹문서
    private String contents;
    // 블로그
    private String blogname;
    // 동영상
    private int play_time;
    private String author;
    private String thumbnail;
    // 카페
    private String cafename;
    // 공통
    private String title;
    private String datetime;
    private String url;
}
