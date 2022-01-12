package com.kingname.api;

import com.kingname.api.common.Constant;
import com.kingname.api.vo.Company;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static com.kingname.api.common.Constant.COMPANY_LIST;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class KakaoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(KakaoApiApplication.class, args);
    }

    @PostConstruct
    public void createSearchWord() throws Exception{
        log.info("=============== READ CSV FILE START ===============");
        Resource resource = new ClassPathResource("static/company/companyList.csv");
        List<String> lines = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8);
        for (String line : lines) {
            try {
                if (line.split(Constant.COMMA)[2] != null && !"".equals(line.split(Constant.COMMA)[2])) {
                    COMPANY_LIST.add(new Company(line.split(Constant.COMMA)[2], line.split(Constant.COMMA)[0]));
                }
            } catch (Exception e) {
                log.info(line);
            }
        }
        log.info("=============== READ CSV FILE END ===============");
    }
}
