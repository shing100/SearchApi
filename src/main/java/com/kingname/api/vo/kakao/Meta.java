package com.kingname.api.vo.kakao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class Meta {

    private boolean is_end;
    private int pageable_count;
    private int total_count;
}
