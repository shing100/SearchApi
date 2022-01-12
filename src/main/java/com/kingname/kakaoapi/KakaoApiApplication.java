package com.kingname.kakaoapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class KakaoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(KakaoApiApplication.class, args);
    }

}
