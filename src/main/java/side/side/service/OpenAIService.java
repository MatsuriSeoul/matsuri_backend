package side.side.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import side.side.model.TourEvent;
import side.side.repository.TourEventRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    @Autowired
    private TourEventRepository tourEventRepository;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private final RestTemplate restTemplate;

    public OpenAIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 지역과 contenttypeid를 매핑하는 맵
    private static final Map<String, String> regionMap = Map.ofEntries(
            Map.entry("서울", "서울"),
            Map.entry("경기", "경기"),
            Map.entry("인천", "인천"),
            Map.entry("대전", "대전"),
            Map.entry("강원", "강원"),
            Map.entry("부산", "부산"),
            Map.entry("울산", "울산"),
            Map.entry("대구", "대구"),
            Map.entry("전남", "전남"),
            Map.entry("전북", "전북"),
            Map.entry("충남", "충남"),
            Map.entry("충북", "충북"),
            Map.entry("경남", "경남"),
            Map.entry("경북", "경북"),
            Map.entry("제주", "제주")
    );

    // 카테고리와 contenttypeid를 매핑하는 맵
    private static final Map<String, String> categoryMap = Map.of(
            "관광지", "12",
            "문화시설", "14",
            "행사", "15",
            "여행코스", "25",
            "레포츠", "28",
            "숙박", "32",
            "쇼핑", "38",
            "음식", "39"
    );

    public String getResponseFromOpenAI(String region, String category) {
        // 프론트엔드에서 전달된 값 확인
        System.out.println("전달된 지역: " + region);
        System.out.println("전달된 카테고리: " + category);

        // 지역과 카테고리를 매핑된 값으로 변환 (소문자 변환)
        String mappedRegion = regionMap.getOrDefault(region.toLowerCase(), null); // 소문자 처리하여 매핑
        String mappedCategory = categoryMap.getOrDefault(category, null);

        // 매핑된 값 확인
        System.out.println("매핑된 지역: " + mappedRegion);
        System.out.println("매핑된 카테고리: " + mappedCategory);

        if (mappedRegion == null || mappedCategory == null) {
            return "잘못된 지역 또는 카테고리입니다. 다시 선택해 주세요.";
        }

        // DB에서 지역과 카테고리에 맞는 데이터를 조회 (부분 검색)
        List<TourEvent> events = tourEventRepository.findByAddr1ContainingAndContenttypeid(mappedRegion, mappedCategory);

        // 데이터가 없을 경우 처리
        StringBuilder eventList = new StringBuilder();
        if (events.isEmpty()) {
            eventList.append(String.format("%s에서 %s에 해당하는 행사가 없습니다.", mappedRegion, category));
        } else {
            eventList.append(String.format("%s에서 열리는 %s에 대한 정보를 알려드릴게요!\n", region, category));
            for (TourEvent event : events) {
                eventList.append(String.format("행사 제목: %s\n이미지: %s\n", event.getTitle(), event.getFirstimage()));
            }
        }

        // OpenAI API 호출 준비
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openaiApiKey);
        headers.set("Content-Type", "application/json");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", eventList.toString())
        ));
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return response.getBody();
    }

}
