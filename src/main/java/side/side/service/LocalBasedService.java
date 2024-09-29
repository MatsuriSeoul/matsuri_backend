package side.side.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocalBasedService {

    private final String serviceKey = "13jkaARutXp/OwAHynRnYjP7BJuMVGIZx2Ki3dRMaDlcBqrfZHC9Zk97LCCuLyKfiR2cVhyWy59t96rPwyWioA==";
    private final String baseUrl = "http://apis.data.go.kr/B551011/KorService1/areaBasedList1";

    public Map<String, Object> getTourismInfo(String region, int subregionCode, int numOfRows, int pageNo) {
        List<Map<String, String>> allEvents = new ArrayList<>();
        boolean moreData = true;

        while (moreData) {
            try {
                RestTemplate restTemplate = new RestTemplate();

                String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                        .queryParam("ServiceKey", serviceKey) // 대소문자 수정
                        .queryParam("pageNo", pageNo)
                        .queryParam("numOfRows", numOfRows)
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "AppTest")
                        .queryParam("arrange", "A")
                        .queryParam("listYN", "Y")
                        .queryParam("contentTypeId", "32") // 숙박시설
                        .queryParam("areaCode", getAreaCode(region))
                        .queryParam("sigunguCode", subregionCode)
                        .queryParam("cat1", "A02")
                        .queryParam("cat2", "")
                        .queryParam("cat3", "")
                        .queryParam("_type", "json")
                        .build()
                        .toUriString();

                System.out.println("API Request URL: " + url); // 디버깅용 로그

                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    JSONObject jsonResponse = new JSONObject(response.getBody());
                    JSONObject responseBody = jsonResponse.getJSONObject("response").getJSONObject("body");
                    JSONObject itemsObj = responseBody.optJSONObject("items");

                    if (itemsObj == null) {
                        moreData = false;
                        continue;
                    }

                    JSONArray items = itemsObj.optJSONArray("item");

                    if (items == null || items.length() == 0) {
                        moreData = false;
                    } else {
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            Map<String, String> event = new HashMap<>();
                            event.put("title", item.optString("title"));
                            event.put("firstimage", item.optString("firstimage"));
                            allEvents.add(event);
                        }
                        int totalCount = responseBody.optInt("totalCount");
                        if (pageNo * numOfRows >= totalCount) {
                            moreData = false;
                        } else {
                            pageNo++;
                        }
                    }
                } else {
                    System.out.println("API Response Status: " + response.getStatusCode()); // 디버깅용 로그
                    moreData = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                moreData = false;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("events", allEvents);
        return result;
    }

    private int getAreaCode(String region) {
        switch (region.toLowerCase()) {
            case "seoul": return 1;
            case "busan": return 6;
            case "daegu": return 4;
            case "incheon": return 2;
            case "gwangju": return 5;
            case "daejeon": return 3;
            case "ulsan": return 7;
            case "sejong": return 8;
            case "gyeonggi": return 31;
            case "gangwon": return 32;
            case "chungbuk": return 33;
            case "chungnam": return 34;
            case "gyeongbuk": return 35;
            case "gyeongnam": return 36;
            case "jeonbuk": return 37;
            case "jeonnam": return 38;
            case "jeju": return 39;
            default: throw new IllegalArgumentException("Invalid region name: " + region);
        }
    }
}
