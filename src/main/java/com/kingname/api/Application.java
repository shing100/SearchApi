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
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void createSearchWord() throws Exception{
        log.info("=============== READ CSV FILE START ===============");
        Resource resource = new ClassPathResource("static/company/company.txt");
        List<String> lines = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8);
        for (String line : lines) {
            try {
                if (line.split(Constant.SEPARATOR)[0] != null && !"".equals(line.split(Constant.SEPARATOR)[1])) {
                    String companyName = line.split(Constant.SEPARATOR)[0];
                     companyName = companyName.replace("(주)", "").trim();
                    COMPANY_LIST.add(new Company(companyName.replace(" ", ""), line.split(Constant.SEPARATOR)[1]));
                }
            } catch (Exception e) {
                log.info(line);
            }
        }
        log.info("=============== READ CSV FILE END ===============");
    }
}
