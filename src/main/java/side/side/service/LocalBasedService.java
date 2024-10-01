package side.side.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.util.UriComponentsBuilder;
import side.side.model.LocalBase;
import side.side.repository.LocalBasedRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocalBasedService {

    @Autowired
    private LocalBasedRepository localBaseRepository;

    private final String serviceKey = "13jkaARutXp/OwAHynRnYjP7BJuMVGIZx2Ki3dRMaDlcBqrfZHC9Zk97LCCuLyKfiR2cVhyWy59t96rPwyWioA==";
    private final String baseUrl = "http://apis.data.go.kr/B551011/KorService1/areaBasedList1";

    // API를 호출하고 결과를 저장하는 메소드
    public List<LocalBase> fetchAndSaveEvents(String region, int numOfRows, String pageNo) {
        List<LocalBase> allEvents = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();

        // API 호출 URL 빌드 (sigunguCode를 제외하고 호출)
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("serviceKey", serviceKey)
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", pageNo)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "AppTest")
                .queryParam("listYN", "Y")
                .queryParam("arrange", "A")
                //.queryParam("contentTypeId", "32")  // 숙박시설 고정
                .queryParam("areaCode", getAreaCode(region))  // 지역 코드만 추가
                .queryParam("_type", "json")
                .build()
                .toUriString();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");

                if (itemsNode.isArray()) {
                    List<LocalBase> events = new ArrayList<>();
                    for (JsonNode node : itemsNode) {
                        LocalBase event = new LocalBase();
                        event.setTitle(node.path("title").asText());
                        event.setAddr1(node.path("addr1").asText());
                        event.setAddr2(node.path("addr2").asText());
                        event.setFirstImage(node.path("firstimage").asText());
                        event.setFirstImage2(node.path("firstimage2").asText());
                        event.setAreaCode(node.path("areacode").asInt());
                        event.setSigunguCode(node.path("sigungucode").asInt());
                        event.setCat1(node.path("cat1").asText());
                        event.setCat2(node.path("cat2").asText());
                        event.setCat3(node.path("cat3").asText());
                        event.setContentTypeId(node.path("contenttypeid").asText());
                        event.setMapX(node.path("mapx").asDouble());
                        event.setMapY(node.path("mapy").asDouble());
                        event.setTelephone(node.path("tel").asText());
                        event.setZipcode(node.path("zipcode").asText());
                        events.add(event);
                    }
                    localBaseRepository.saveAll(events);
                    allEvents.addAll(events);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return allEvents;
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
