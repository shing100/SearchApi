package com.kingname.kakaoapi.scheduler;

import com.kingname.kakaoapi.service.KakaoSearchService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class KakaoSearchScheduler {

    private final KakaoSearchService kakaoSearchService;

    private final String COMMA = ",";

    @Scheduled(fixedDelay = 100000000)
    public void batchKakaoBuzCount() throws Exception {
        log.info("=============== KAKAO_BUZZ_COUNT START ===============");
        List<Company> companyList = new ArrayList<>();

        log.info("=============== READ CSV FILE START ===============");
        Resource resource = new ClassPathResource("static/company/companyList.csv");
        List<String> lines = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8);
        for (String line : lines) {
            try {
                companyList.add(new Company(line.split(COMMA)[2], line.split(COMMA)[0]));
            } catch (Exception e) {
                log.info(line.split(COMMA)[2] + " : " + line.split(COMMA)[0]);
            }
        }
        log.info("=============== READ CSV FILE END ===============");

        for (Company company : companyList) {
            kakaoSearchService.saveKakaoBuzzCount(company.getCompanyName(), company.getCsn());
        }
        log.info("=============== KAKAO_BUZZ_COUNT END ===============");
    }

    @Data
    @AllArgsConstructor
    static class Company {
        private String companyName;
        private String csn;
    }
}
