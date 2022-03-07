package com.kingname.api.controller;

import com.kingname.api.common.Utils;
import com.kingname.api.service.KeywordBuzzService;
import com.kingname.api.vo.Buzz;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class KeywordBuzzController {

    private final KeywordBuzzService keywordBuzzService;

    @GetMapping("/company-chart-analyzer")
    public ResponseEntity companyBuzzCount(@RequestParam(value = "searchWord") String searchWord, String type) throws Exception {
        try {
            List<Buzz> result = keywordBuzzService.getCompanyBuzzHistogram(searchWord, type);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
