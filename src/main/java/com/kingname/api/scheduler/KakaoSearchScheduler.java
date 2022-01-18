package com.kingname.api.scheduler;

import com.kingname.api.service.KakaoSearchService;
import com.kingname.api.vo.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.kingname.api.common.Constant.COMPANY_LIST;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoSearchScheduler {

    private final KakaoSearchService kakaoSearchService;

    @Scheduled(fixedDelay = 100000000)
    public void batchKakaoBuzCount() throws Exception {
        log.info("=============== KAKAO_BUZZ_COUNT START ===============");
        for (Company company : COMPANY_LIST) {
            kakaoSearchService.saveKakaoBuzzCount(company.getCompanyName(), company.getCsn());
        }
        log.info("=============== KAKAO_BUZZ_COUNT END ===============");
    }
}
