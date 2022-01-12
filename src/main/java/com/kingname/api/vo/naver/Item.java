package com.kingname.api.vo.naver;


import com.kingname.api.common.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class Item {

    // 뉴스
    private String title;
    private String originallink;
    private String description;
    private String pubDate;

    // 블로그
    private String postdate;
    private String link;
    private String bloggername;
    private String bloggerlink;

    public String getDate() {
        if (this.pubDate != null && !"".equals(this.pubDate)) {
            return Utils.convertNaverDateToDateFormat(this.pubDate, "yyyyMMdd");
        } else if (this.postdate != null && !"".equals(this.postdate)) {
            return this.postdate;
        } else {
            return null;
        }
    }
}
