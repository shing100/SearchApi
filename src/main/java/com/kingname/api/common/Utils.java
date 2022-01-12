package com.kingname.api.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Slf4j
public class Utils {

    public static String createIndexName(String name, String date) {
        String IndexPattern = "buzz-".concat(name).concat("-YYYYMM");
        return IndexPattern.replace("YYYYMM", date.substring(0,6));
    }

    public static String getNowDateFormat(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Calendar time = Calendar.getInstance();
        return sdf.format(time.getTime());
    }

    public static String convertNaverDateToDateFormat(String dateStr, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
        try {
            Date date = dateFormat.parse(dateStr);
            return new SimpleDateFormat(format).format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date convertStringToDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getDateStrByDateTime(String dateTime, String format) {
        try {
            DateFormat df = new SimpleDateFormat(format);
            Date date = df.parse(dateTime.substring(0, 10));
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            String yearStr = String.valueOf(localDate.getYear());
            String monthStr = localDate.getMonthValue() < 10 ? "0" + localDate.getMonthValue() : String.valueOf(localDate.getMonthValue());
            String dayStr = localDate.getDayOfMonth() < 10 ? "0" + localDate.getDayOfMonth() : String.valueOf(localDate.getDayOfMonth());
            return yearStr + monthStr + dayStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static List<Map.Entry<Integer, Integer>> sortMapByKey(Map<Integer, Integer> map) {
        List<Map.Entry<Integer, Integer>> entries = new LinkedList<>(map.entrySet());
        entries.sort((o1, o2) -> o2.getKey().compareTo(o1.getKey()));
        return entries;
    }

    public static MultiValueMap<String, String> convertRequestVO(ObjectMapper objectMapper, Object dto) { // (2)
        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            Map<String, String> map = objectMapper.convertValue(dto, new TypeReference<Map<String, String>>() {}); // (3)
            params.setAll(map); // (4)

            return params;
        } catch (Exception e) {
            log.error("Url Parameter 변환중 오류가 발생했습니다. requestDto={}", dto, e);
            throw new IllegalStateException("Url Parameter 변환중 오류가 발생했습니다.");
        }
    }
}
