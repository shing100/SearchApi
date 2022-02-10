package com.kingname.api.scheduler;

import com.kingname.api.common.Constant;
import com.kingname.api.service.KakaoSearchService;
import com.kingname.api.service.NaverSearchService;
import com.kingname.api.vo.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BuzzScheduler {

    private final KakaoSearchService kakaoSearchService;
    private final NaverSearchService naverSearchService;

    @Scheduled(fixedDelay = 100000000)
    public void batchBuzzCount() throws Exception {
        List<Company> companyList = new ArrayList<>();
        Resource resource = new ClassPathResource("static/company/company.txt");
        List<String> lines = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8);

        for (String line : lines) {
            String companyName = line.split(Constant.SEPARATOR)[0]
                    .replace("(주)", "")
                    .replace("(우)", "")
                    .replace(" ", "").trim();
            String csn = line.split(Constant.SEPARATOR)[1];
            companyList.add(new Company(companyName, csn));
        }

        log.info("=============== KEYWORD_BUZZ_COUNT START ===============");
        for (Company company : companyList) {
            kakaoSearchService.saveKakaoBuzzCount(company.getCompanyName(), company.getCsn());
            naverSearchService.saveNaverBuzzCount(company.getCompanyName(), company.getCsn());
        }
        log.info("=============== KEYWORD_BUZZ_COUNT END ===============");
    }
}
