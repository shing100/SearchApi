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

import java.util.*;

@Getter @Setter @ToString
@Document(indexName = "#{@elasticsearchIndex.getIndexName()}")
public class KakaoBuzz {

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
    public static KakaoBuzz create(String csn, String type, String searchKeyword, Map.Entry<Integer, Integer> entry) {
        KakaoBuzz kakaoBuzz = new KakaoBuzz();
        kakaoBuzz.setCsn(csn);
        kakaoBuzz.setType("kakao_" + type);
        kakaoBuzz.setSearchKeyword(searchKeyword);
        kakaoBuzz.setEvent_date(Date.from(Objects.requireNonNull(Utils.convertStringToDate(String.valueOf(entry.getKey())))
                .toInstant().plusSeconds(3600 * 9L)));
        kakaoBuzz.setCount(entry.getValue());
        kakaoBuzz.setId(csn + "_kakao_" + type + "_" + entry.getKey());
        return kakaoBuzz;
    }
}

