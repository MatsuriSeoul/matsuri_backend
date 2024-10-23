package side.side.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import side.side.model.*;
import side.side.repository.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class OpenAIService {

    @Autowired
    private UserClickLogService userClickLogService;

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
        String mappedRegion = regionMap.getOrDefault(region.toLowerCase(), null);
        String mappedCategory = categoryMap.getOrDefault(category, null);

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
        List<TravelCourseDetail> travelCourseDetails = travelCourseDetailRepository.findByAddr1Containing(mappedRegion);
        List<LeisureSportsEvent> leisureSportsEvents = leisureSportsEventRepository.findByAddr1ContainingAndContenttypeid(mappedRegion, mappedCategory);

        // 조회 결과 통합
        List<Map<String, String>> combinedResults = new ArrayList<>();

        // OpenAI API 호출을 병렬로 처리
        List<CompletableFuture<Map<String, String>>> futures = new ArrayList<>();

        // TourEvent 처리
        events.forEach(event -> futures.add(
                CompletableFuture.supplyAsync(() -> createResultMap(event.getTitle(), event.getFirstimage()))
        ));

        // CulturalFacility 처리
        facilities.forEach(facility -> futures.add(
                CompletableFuture.supplyAsync(() -> createResultMap(facility.getTitle(), facility.getFirstimage()))
        ));

        // 쇼핑 이벤트
        shoppings.forEach(shopping -> futures.add(
                CompletableFuture.supplyAsync(() -> createResultMap(shopping.getTitle(), shopping.getFirstimage()))
        ));

        // 음식 이벤트
        foods.forEach(food -> futures.add(
                CompletableFuture.supplyAsync(() -> createResultMap(food.getTitle(), food.getFirstimage()))
        ));

        // 관광지 이벤트
        touristattractions.forEach(touristAttraction -> futures.add(
                CompletableFuture.supplyAsync(() -> createResultMap(touristAttraction.getTitle(), touristAttraction.getFirstimage()))
        ));

        // 숙박 이벤트
        locals.forEach(local -> futures.add(
                CompletableFuture.supplyAsync(() -> createResultMap(local.getTitle(), local.getFirstimage()))
        ));

        // 레저스포츠 이벤트
        leisureSportsEvents.forEach(leisureSportsEvent -> futures.add(
                CompletableFuture.supplyAsync(() -> createResultMap(leisureSportsEvent.getTitle(), leisureSportsEvent.getFirstimage()))
        ));

        // 여행코스
        travelCourseDetails.forEach(travelCourseDetail -> futures.add(
                CompletableFuture.supplyAsync(() -> createResultMap(travelCourseDetail.getTitle(), travelCourseDetail.getOverview()))
        ));

        // 비동기 작업이 모두 끝나기를 기다림
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 결과 수집
        futures.forEach(future -> combinedResults.add(future.join()));

        if (combinedResults.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", String.format("%s에서 %s에 해당하는 데이터가 없습니다.", mappedRegion, mappedCategory)));
        }

        List<Map<String, String>> limitedResults = combinedResults.stream().limit(2).toList();

        return ResponseEntity.ok(Map.of("events", limitedResults));
    }

    // OpenAI API를 사용하여 제목에 대한 추천 문구 생성 (캐시 적용)
    @Cacheable(value = "recommendations", key = "#title")
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
        requestBody.put("max_tokens", 100);
        requestBody.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            return jsonResponse.get("choices").get(0).get("message").get("content").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return "추천 문구 생성에 실패했습니다.";
        }
    }

    // 추천 데이터를 반환하는 비동기 작업
    private Map<String, String> createResultMap(String title, String imageUrl) {
        String recommendation = getRecommendationForTitle(title);
        return Map.of(
                "title", title,
                "image", imageUrl != null ? imageUrl : "이미지 없음",
                "recommendation", recommendation
        );
    }

    // 사용자별 맞춤 추천 문구를 포함한 추천 데이터 반환
    public List<Map<String, Object>> getPersonalizedRecommendations(Long userId) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        String mostViewedCategory = userClickLogService.findMostViewedCategoryByUser(userId);

        if (mostViewedCategory != null) {
            List<Map<String, Object>> categoryData = userClickLogService.getCategoryData(mostViewedCategory);

            List<CompletableFuture<Map<String, Object>>> futures = categoryData.stream()
                    .map(event -> CompletableFuture.supplyAsync(() -> {
                        String title = event.get("title").toString();
                        String aiRecommendation = getRecommendationForTitle(title);
                        event.put("aiRecommendation", aiRecommendation);
                        return event;
                    }))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            futures.forEach(future -> recommendations.add(future.join()));
        }

        return recommendations;
    }
}

