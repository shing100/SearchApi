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

    @Scheduled(fixedDelay = 100000000)
    public void batchKakaoBuzCount() throws Exception {
        log.info("=============== COM_ANALYSIS_KAKAO_BUZZ_COUNT START ===============");
        List<Company> companyList = new ArrayList<>();

        Resource resource = new ClassPathResource("static/company/searchList.txt");
        List<String> lines = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8);
        for (String line : lines) {
            companyList.add(new Company(line.split(",")[0], line.split(",")[1]));
        }

        for (Company company : companyList) {
            kakaoSearchService.saveKakaoBuzzCount(company.getCompanyName(), company.getCsn());
        }
        log.info("=============== COM_ANALYSIS_KAKAO_BUZZ_COUNT END ===============");
    }

    @Data
    @AllArgsConstructor
    static class Company {
        private String companyName;
        private String csn;
    }
}
