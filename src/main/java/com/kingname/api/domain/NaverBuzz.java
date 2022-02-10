package com.kingname.api.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kingname.api.common.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Getter @Setter @ToString
@Document(indexName = "#{@elasticsearchIndex.getIndexName()}")
public class NaverBuzz {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String csn;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Keyword)
    private String searchKeyword;

    @Field(type = FieldType.Long)
    private int count;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
    @Field(type = FieldType.Date, format = DateFormat.basic_date)
    private Date event_date;

    @Field(type = FieldType.Keyword)
    private String company_nm = "";

    @Field(type = FieldType.Long)
    private int com_ind = 0;

    @Field(type = FieldType.Keyword)
    private String com_ind_nm = "";

    @Field(type = FieldType.Long)
    private int com_area = 0;

    @Field(type = FieldType.Keyword)
    private String com_area_nm = "";

    // entry dateStr(yyyyMMdd), count
    public static NaverBuzz create(String csn, String type, String searchKeyword, Map.Entry<Integer, Integer> entry) {
        NaverBuzz naverBuzz = new NaverBuzz();
        naverBuzz.setCsn(csn);
        naverBuzz.setType("naver" + type);
        naverBuzz.setSearchKeyword(searchKeyword);
        naverBuzz.setEvent_date(Date.from(Objects.requireNonNull(Utils.convertStringToDate(String.valueOf(entry.getKey())))
                .toInstant().plusSeconds(3600 * 9L)));
        naverBuzz.setCount(entry.getValue());
        naverBuzz.setId(csn + "_naver_" + type + "_" + entry.getKey());
        return naverBuzz;
    }

    public boolean validateThisYear() {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            String today = Utils.getNowDateFormat("yyyyMMdd");
            String yearStartDay = today.substring(0, 4).concat("0101");
            int compare = event_date.compareTo(simpleDateFormat.parse(yearStartDay));
            return compare > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
