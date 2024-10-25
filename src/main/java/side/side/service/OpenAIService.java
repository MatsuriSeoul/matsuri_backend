package side.side.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
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

    // 사용자가 입력한 지역, 여러 카테고리, 기간에 맞는 일정을 생성
    public Map<String, Object> generateAIPlan(String region, List<String> categories, String duration) {
        // 1. 지역 매핑
        String mappedRegion = regionMap.getOrDefault(region, null);
        if (mappedRegion == null) {
            throw new IllegalArgumentException("잘못된 지역입니다.");
        }

        // 2. 카테고리별 데이터 병합
        List<Map<String, String>> eventList = new ArrayList<>();

        for (String category : categories) {
            String mappedCategory = categoryMap.getOrDefault(category, null);
            if (mappedCategory != null) {
                eventList.addAll(fetchEventsForCategory(mappedRegion, mappedCategory));
            }
        }

        if (eventList.isEmpty()) {
            throw new IllegalArgumentException("해당 지역 및 카테고리에 맞는 데이터가 없습니다.");
        }

        // 3. 이벤트 목록을 필터링하여 같은 지역에 있는 데이터만 유지 (같은 구에 속하는 데이터만)
        List<Map<String, String>> filteredEvents = filterEventsByAddress(eventList);

        // 4. 여행 기간에 따른 이벤트 제한
        int maxEvents = getMaxEventsBasedOnDuration(duration);
        List<Map<String, String>> limitedEvents = filteredEvents.stream().limit(maxEvents).collect(Collectors.toList());

        // 5. OpenAI API 호출 프롬프트 생성
        String prompt = String.format(
                "사용자가 %s 지역에서 %s 카테고리로 %s 동안 여행할 계획입니다. "
                        + "다음과 같은 이벤트를 추천해 주세요: %s",
                region, String.join(", ", categories), duration, limitedEvents.stream().map(event -> event.get("title")).collect(Collectors.joining(", "))
        );

        // 6. OpenAI API 호출
        String aiResponse = callOpenAI(prompt);

        // 7. 결과 반환
        Map<String, Object> result = new HashMap<>();
        result.put("events", limitedEvents);
        result.put("aiResponse", aiResponse);

        return result;
    }

    // 카테고리에 따른 이벤트를 조회하는 메서드
    private List<Map<String, String>> fetchEventsForCategory(String region, String mappedCategory) {
        List<Map<String, String>> eventList = new ArrayList<>();

        // 각 카테고리에 맞는 데이터를 조회
        List<FoodEvent> foodEvents = foodEventRepository.findByAddr1ContainingAndContenttypeid(region, mappedCategory);
        List<TourEvent> tourEvents = tourEventRepository.findByAddr1ContainingAndContenttypeid(region, mappedCategory);
        List<CulturalFacility> culturalFacilities = culturalFacilityRepository.findByAddr1ContainingAndContenttypeid(region, mappedCategory);
        List<ShoppingEvent> shoppingEvents = shoppingEventRepository.findByAddr1ContainingAndContenttypeid(region, mappedCategory);
        List<TouristAttraction> touristAttractions = touristAttractionRepository.findByAddr1ContainingAndContenttypeid(region, mappedCategory);
        List<LeisureSportsEvent> leisureSportsEvents = leisureSportsEventRepository.findByAddr1ContainingAndContenttypeid(region, mappedCategory);

        // 각 이벤트를 맵으로 변환하여 추가
        foodEvents.forEach(event -> eventList.add(createEventMap(event.getTitle(), event.getFirstimage(), event.getAddr1())));
        tourEvents.forEach(event -> eventList.add(createEventMap(event.getTitle(), event.getFirstimage(), event.getAddr1())));
        culturalFacilities.forEach(event -> eventList.add(createEventMap(event.getTitle(), event.getFirstimage(), event.getAddr1())));
        shoppingEvents.forEach(event -> eventList.add(createEventMap(event.getTitle(), event.getFirstimage(), event.getAddr1())));
        touristAttractions.forEach(event -> eventList.add(createEventMap(event.getTitle(), event.getFirstimage(), event.getAddr1())));
        leisureSportsEvents.forEach(event -> eventList.add(createEventMap(event.getTitle(), event.getFirstimage(), event.getAddr1())));

        return eventList;
    }
    // 거리 기준으로 이벤트 필터링 (같은 구에 속하는지 여부)
    private List<Map<String, String>> filterEventsByAddress(List<Map<String, String>> events) {
        // 예시 필터링 로직 (여기서는 addr1 필드를 기반으로 같은 구에 있는지 확인)
        String baseAddr = events.get(0).get("addr1");  // 첫 번째 이벤트의 주소 기준으로 필터링
        return events.stream()
                .filter(event -> event.get("addr1").contains(baseAddr.split(" ")[1])) // 구 기준으로 비교
                .collect(Collectors.toList());
    }
    // 여행 기간에 따라 최대 이벤트 개수 결정
    private int getMaxEventsBasedOnDuration(String duration) {
        switch (duration) {
            case "당일":
                return 3; // 당일 여행의 경우 3개 이벤트 추천
            case "1박 2일":
                return 5; // 1박 2일은 최대 5개 추천
            case "2박 3일":
                return 7; // 2박 3일의 경우 7개 이벤트 추천
            default:
                return 3;
        }
    }

    // 이벤트 데이터를 맵으로 변환하는 유틸리티 메서드
    private Map<String, String> createEventMap(String title, String imageUrl, String addr1) {
        Map<String, String> eventMap = new HashMap<>();
        eventMap.put("title", title);
        eventMap.put("image", imageUrl != null ? imageUrl : "이미지 없음");
        eventMap.put("addr1", addr1);
        return eventMap;
    }

    // OpenAI API 호출 로직
    private String callOpenAI(String prompt) {
        String url = "https://api.openai.com/v1/chat/completions";  // 엔드포인트 수정

        // HTTP 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openaiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // OpenAI 요청 내용 설정
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "당신은 여행 플래너입니다. 사용자가 입력한 정보를 바탕으로 여행 계획을 작성해 주세요."),
                Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.7);

        // HTTP 요청 생성
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // OpenAI API로 POST 요청 보내기
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // 응답 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            return jsonResponse.get("choices").get(0).get("message").get("content").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return "OpenAI API 호출 중 오류가 발생했습니다.";
        }
    }
}