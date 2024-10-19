package side.side.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import side.side.model.*;
import side.side.repository.*;

import java.util.*;

@Service
public class OpenAIService {

    @Autowired
    private TourEventRepository tourEventRepository;

    @Autowired
    private CulturalFacilityRepository culturalFacilityRepository;

    @Autowired
    private ShoppingEventRepository shoppingEventRepository;

    @Autowired
    private FoodEventRepository foodEventRepository;

    @Autowired
    private TouristAttractionRepository touristAttractionRepository;

    @Autowired
    private LeisureSportsEventRepository leisureSportsEventRepository;

    @Autowired
    private TravelCourseDetailRepository travelCourseDetailRepository;

    @Autowired
    private LocalEventRepository localEventRepository;

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
            Map.entry("전남", "전라남도"),
            Map.entry("전북", "전북특별자치도"),
            Map.entry("충남", "충청남도"),
            Map.entry("충북", "충청북도"),
            Map.entry("경남", "경상남도"),
            Map.entry("경북", "경상북도"),
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

    public ResponseEntity<Map<String, Object>> getResponse(String region, String category) {
        // 프론트엔드에서 전달된 값 확인
        System.out.println("전달된 지역: " + region);
        System.out.println("전달된 카테고리: " + category);

        // 지역과 카테고리를 매핑된 값으로 변환 (소문자 변환)
        String mappedRegion = regionMap.getOrDefault(region.toLowerCase(), null);
        String mappedCategory = categoryMap.getOrDefault(category, null);

        // 매핑된 값 확인
        System.out.println("매핑된 지역: " + mappedRegion);
        System.out.println("매핑된 카테고리: " + mappedCategory);

        if (mappedRegion == null || mappedCategory == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "잘못된 지역 또는 카테고리입니다."));
        }

        // DB에서 지역과 카테고리에 맞는 데이터를 조회
        List<TourEvent> events = tourEventRepository.findByAddr1ContainingAndContenttypeid(mappedRegion, mappedCategory);
        List<CulturalFacility> facilities = culturalFacilityRepository.findByAddr1ContainingAndContenttypeid(mappedRegion, mappedCategory);
        List<ShoppingEvent> shoppings = shoppingEventRepository.findByAddr1ContainingAndContenttypeid(mappedRegion, mappedCategory);
        List<FoodEvent> foods = foodEventRepository.findByAddr1ContainingAndContenttypeid(mappedRegion, mappedCategory);
        List<TouristAttraction> touristattractions = touristAttractionRepository.findByAddr1ContainingAndContenttypeid(mappedRegion, mappedCategory);
        List<LocalEvent> locals = localEventRepository.findByAddr1ContainingAndContenttypeid(mappedRegion, mappedCategory);
        List<TravelCourseDetail> travelCourseDetails = travelCourseDetailRepository.findByAddr1Containing(mappedRegion); // 여행코스 관련 데이터 조회
        List<LeisureSportsEvent> leisureSportsEvents = leisureSportsEventRepository.findByAddr1ContainingAndContenttypeid(mappedRegion, mappedCategory);

        // 조회 결과 통합
        List<Map<String, String>> combinedResults = new ArrayList<>();

        // TourEvent 처리
        for (TourEvent event : events) {
            String recommendation = getRecommendationForTitle(event.getTitle()); // OpenAI API 호출
            combinedResults.add(Map.of(
                    "title", event.getTitle(),
                    "image", event.getFirstimage() != null ? event.getFirstimage() : "이미지 없음",
                    "recommendation", recommendation  // 추천 문구 추가
            ));
        }

        // CulturalFacility 처리
        for (CulturalFacility facility : facilities) {
            String recommendation = getRecommendationForTitle(facility.getTitle()); // OpenAI API 호출
            combinedResults.add(Map.of(
                    "title", facility.getTitle(),
                    "image", facility.getFirstimage() != null ? facility.getFirstimage() : "이미지 없음",
                    "recommendation", recommendation  // 추천 문구 추가
            ));
        }

        // 쇼핑 이벤트
        for (ShoppingEvent shopping : shoppings) {
            String recommendation = getRecommendationForTitle(shopping.getTitle()); // OpenAI API 호출
            combinedResults.add(Map.of(
                    "title", shopping.getTitle(),
                    "image", shopping.getFirstimage() != null ? shopping.getFirstimage() : "이미지없음",
                    "recommendation", recommendation  // 추천 문구 추가
            ));
        }

        // 음식 이벤트
        for (FoodEvent food : foods) {
            String recommendation = getRecommendationForTitle(food.getTitle()); // OpenAI API 호출
            combinedResults.add(Map.of(
                    "title", food.getTitle(),
                    "image", food.getFirstimage() != null ? food.getFirstimage() : "이미지없음",
                    "recommendation", recommendation  // 추천 문구 추가
            ));
        }

        // 관광지 이벤트
        for (TouristAttraction touristAttraction : touristattractions) {
            String recommendation = getRecommendationForTitle(touristAttraction.getTitle()); // OpenAI API 호출
            combinedResults.add(Map.of(
                    "title", touristAttraction.getTitle(),
                    "image", touristAttraction.getFirstimage() != null ? touristAttraction.getFirstimage() : "이미지없음",
                    "recommendation", recommendation  // 추천 문구 추가
            ));
        }

        // 숙박 이벤트
        for (LocalEvent local : locals) {
            String recommendation = getRecommendationForTitle(local.getTitle()); // OpenAI API 호출
            combinedResults.add(Map.of(
                    "title", local.getTitle(),
                    "image", local.getFirstimage() != null ? local.getFirstimage() : "이미지없음",
                    "recommendation", recommendation  // 추천 문구 추가
            ));
        }

        // 레저스포츠
        for (LeisureSportsEvent leisureSportsEvent : leisureSportsEvents) {
            String recommendation = getRecommendationForTitle(leisureSportsEvent.getTitle()); // OpenAI API 호출
            combinedResults.add(Map.of(
                    "title", leisureSportsEvent.getTitle(),
                    "image", leisureSportsEvent.getFirstimage() != null ? leisureSportsEvent.getFirstimage() : "이미지없음",
                    "recommendation", recommendation  // 추천 문구 추가
            ));
        }

        // 여행코스
        for (TravelCourseDetail travelcoursedetail : travelCourseDetails) {
            String recommendation = getRecommendationForTitle(travelcoursedetail.getTitle()); // OpenAI API 호출
            combinedResults.add(Map.of(
                    "title", travelcoursedetail.getTitle(),
                    "overview", travelcoursedetail.getOverview(),
                    "recommendation", recommendation  // 추천 문구 추가
            ));
        }

        // 조회된 결과가 없을 경우 처리
        if (combinedResults.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", String.format("%s에서 %s에 해당하는 데이터가 없습니다.", mappedRegion, category)));
        }

        // 모든 카테고리 결과 클라이언트에 전달
        List<Map<String, String>> limitedResults = combinedResults.stream().limit(2).toList();

        return ResponseEntity.ok(Map.of(
                "events", limitedResults
        ));
    }

    // OpenAI API를 사용하여 제목에 대한 추천 문구 생성
    public String getRecommendationForTitle(String title) {
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openaiApiKey);
        headers.set("Content-Type", "application/json");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "당신은 여행 제목에 대한 사용자가 매력적으로 느끼게 제목을 입력해주는 조수에요."),
                Map.of("role", "user", "content", "다음 제목에 대한 추천을 생성해 주세요: " + title)
        ));
        requestBody.put("max_tokens", 100);  // 간결한 문구를 위해 토큰 제한
        requestBody.put("temperature", 0.7);  // 창의적인 응답을 위해

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // OpenAI의 응답을 받아 추천 문구로 반환
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            String recommendation = jsonResponse.get("choices").get(0).get("message").get("content").asText();

            return recommendation;
        } catch (Exception e) {
            e.printStackTrace();
            return "추천 문구 생성에 실패했습니다.";
        }
    }
}
