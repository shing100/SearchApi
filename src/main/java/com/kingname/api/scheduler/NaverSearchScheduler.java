package com.kingname.api.scheduler;

import com.kingname.api.service.NaverSearchService;
import com.kingname.api.vo.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.kingname.api.common.Constant.COMPANY_LIST;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverSearchScheduler {

    private final NaverSearchService naverSearchService;

    @Scheduled(fixedDelay = 100000000)
    public void batchNaverBuzCount() throws Exception {
        log.info("=============== NAVER_BUZZ_COUNT START ===============");
        for (Company company : COMPANY_LIST) {
            naverSearchService.saveNaverBuzzCount(company.getCompanyName(), company.getCsn());
        }
        log.info("=============== NAVER_BUZZ_COUNT END ===============");
    }
}
