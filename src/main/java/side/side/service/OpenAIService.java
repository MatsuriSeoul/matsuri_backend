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
                CompletableFuture.supplyAsync(() -> createResultMap(event.getTitle(), event.getFirstimage(), event.getContentid(), event.getContenttypeid()))
        ));

        // CulturalFacility 처리
        facilities.forEach(facility -> futures.add(
                CompletableFuture.supplyAsync(() -> createResultMap(facility.getTitle(), facility.getFirstimage(), facility.getContentid(), facility.getContenttypeid()))
        ));

        // 쇼핑 이벤트
        shoppings.forEach(shopping -> futures.add(
                CompletableFuture.supplyAsync(() -> createResultMap(shopping.getTitle(), shopping.getFirstimage(), shopping.getContentid(), shopping.getContenttypeid()))
        ));

        // 음식 이벤트
        foods.forEach(food -> futures.add(
                CompletableFuture.supplyAsync(() -> createResultMap(food.getTitle(), food.getFirstimage(), food.getContentid(), food.getContenttypeid()))
        ));

        // 관광지 이벤트
        touristattractions.forEach(touristAttraction -> futures.add(
                CompletableFuture.supplyAsync(() -> createResultMap(touristAttraction.getTitle(), touristAttraction.getFirstimage(), touristAttraction.getContentid(), touristAttraction.getContenttypeid()))
        ));

        // 숙박 이벤트
        locals.forEach(local -> futures.add(
                CompletableFuture.supplyAsync(() -> createResultMap(local.getTitle(), local.getFirstimage(), local.getContentid(), local.getContenttypeid()))
        ));

        // 레저스포츠 이벤트
        leisureSportsEvents.forEach(leisureSportsEvent -> futures.add(
                CompletableFuture.supplyAsync(() -> createResultMap(leisureSportsEvent.getTitle(), leisureSportsEvent.getFirstimage(), leisureSportsEvent.getContentid(), leisureSportsEvent.getContenttypeid()))
        ));

        // 여행코스
        travelCourseDetails.forEach(travelCourseDetail -> futures.add(
                CompletableFuture.supplyAsync(() -> createResultMap(travelCourseDetail.getTitle(), travelCourseDetail.getOverview(), travelCourseDetail.getContentid(), travelCourseDetail.getContenttypeid()))
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
        requestBody.put("max_tokens", 500);
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
    private Map<String, String> createResultMap(String title, String imageUrl, String contentid, String contenttypeid) {
        String recommendation = getRecommendationForTitle(title);
        return Map.of(
                "title", title,
                "image", imageUrl != null ? imageUrl : "이미지 없음",
                "recommendation", recommendation,
                "contentid", contentid,
                "contenttypeid", contenttypeid
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
        String mappedRegion = regionMap.getOrDefault(region, null);
        if (mappedRegion == null) {
            throw new IllegalArgumentException("잘못된 지역입니다.");
        }

        // 선택된 카테고리에서 이벤트를 조회
        // 카테고리별로 이벤트 수집 후 이미지가 있는 것을 우선으로 정렬
        List<Map<String, String>> eventList = new ArrayList<>();
        for (String category : categories) {
            String mappedCategory = categoryMap.getOrDefault(category, null);
            if (mappedCategory != null) {
                List<Map<String, String>> events = fetchEventsForCategory(mappedRegion, mappedCategory);
                eventList.addAll(prioritizeEventsWithImages(events));  // 이미지 우선 정렬
            }
        }


        // 가까운 거리로 이벤트를 그룹화
        if (duration.equals("당일")) {
            eventList = groupEventsByProximity(eventList, 5.0); // 당일 여행의 경우, 5km 이내로 제한
        } else {
            eventList = groupEventsByProximity(eventList, 10.0); // 1박 2일 이상 여행의 경우, 10km 이내로 제한
        }



        // 첫 이벤트를 기준으로 거리 제한 적용 (예: 5km 이내)
        if (!eventList.isEmpty()) {
            Map<String, String> baseEvent = eventList.get(0);
            double baseLat = Double.parseDouble(baseEvent.get("mapx"));
            double baseLon = Double.parseDouble(baseEvent.get("mapy"));
            double maxDistance = 5.0; // 거리 제한 (단위: km)

            eventList = filterEventsByDistance(eventList, baseLat, baseLon, maxDistance);
        }

        // 최대 이벤트 수 제한
        // 여행 기간에 따라 최대 이벤트 수 결정
        int maxEvents = getMaxEventsBasedOnDuration(duration);
        eventList = eventList.stream().limit(maxEvents).collect(Collectors.toList());

        // 여행이 1박 2일 이상이면 숙박 시설 추가
        if (!categories.contains("숙박") && (duration.equals("1박 2일") || duration.equals("2박 3일"))) {
            String accommodationCategory = categoryMap.get("숙박");
            List<Map<String, String>> accommodations = fetchEventsForCategory(mappedRegion, accommodationCategory);

            // 숙박 시설 정보에 이미지가 있는지 확인 후 추가
            if (!eventList.isEmpty() && !accommodations.isEmpty()) {  // eventList와 accommodations가 비어있지 않은지 확인
                Map<String, String> selectedAccommodation = findNearestAccommodation(eventList.get(eventList.size() - 1), accommodations);
                eventList.add(selectedAccommodation);  // 마지막에 숙박시설 추가
            }
        }

        // 일정 나누기
        LinkedHashMap<String, List<Map<String, String>>> dayPlans = createDayPlans(eventList, duration);

        // 각 일차에 숙박 추가
        addAccommodationToDayPlans(dayPlans, mappedRegion, duration);

        // OpenAI API 호출용 프롬프트 생성
        String prompt = String.format(
                "사용자가 %s 지역에서 %s 카테고리로 %s 동안 여행할 계획입니다. 추천 이벤트 목록은 다음과 같습니다: %s",
                region, String.join(", ", categories), duration, eventList.stream().map(e -> e.get("title")).collect(Collectors.joining(", "))
        );

        // OpenAI API 호출
        String aiResponse = callOpenAI(prompt);

        // 결과 반환
        Map<String, Object> result = new HashMap<>();
        result.put("events", eventList);
        result.put("dayPlans", dayPlans); // 나누어진 일정을 반환
        result.put("aiResponse", aiResponse);

        return result;
    }
    // 일정 정보를 문자열로 변환하여 프롬프트에 삽입하는 메서드
    private String dayPlansToString(LinkedHashMap<String, List<Map<String, String>>> dayPlans) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<Map<String, String>>> entry : dayPlans.entrySet()) {
            sb.append(entry.getKey()).append(":\n");
            for (Map<String, String> event : entry.getValue()) {
                sb.append("  - ").append(event.get("title")).append(": ").append(event.get("recommendation")).append("\n");
            }
        }
        return sb.toString();
    }


    // 일정 분할 함수에서 일차 표기 수정
    private LinkedHashMap<String, List<Map<String, String>>> createDayPlans(List<Map<String, String>> events, String duration) {
        LinkedHashMap<String, List<Map<String, String>>> dayPlans = new LinkedHashMap<>();
        int days = duration.equals("당일") ? 1 : duration.equals("1박 2일") ? 2 : 3;

        // 일정 레이블 생성
        for (int i = 0; i < days; i++) {
            String dayLabel = (duration.equals("당일") && i == 0) ? "당일" : (i + 1) + "일차";
            dayPlans.put(dayLabel, new ArrayList<>());
        }

        // 이벤트를 일정에 배분
        for (int i = 0; i < events.size(); i++) {
            int dayIndex = Math.min(i / (events.size() / days), days - 1);
            String dayLabel = (duration.equals("당일") && dayIndex == 0) ? "당일" : (dayIndex + 1) + "일차";
            dayPlans.get(dayLabel).add(events.get(i));
        }

        return dayPlans;
    }

    // 이벤트를 정렬할 때 firstimage가 있는 데이터를 우선으로 정렬하는 메서드 추가
    private List<Map<String, String>> prioritizeEventsWithImages(List<Map<String, String>> events) {
        return events.stream()
                .sorted((e1, e2) -> {
                    boolean hasImage1 = e1.get("firstimage") != null && !e1.get("firstimage").isEmpty();
                    boolean hasImage2 = e2.get("firstimage") != null && !e2.get("firstimage").isEmpty();
                    return Boolean.compare(hasImage2, hasImage1);  // 이미지가 있는 데이터 우선
                })
                .collect(Collectors.toList());
    }

    // 각 날짜에 숙박 시설을 가하는 메서드
    private void addAccommodationToDayPlans(LinkedHashMap<String, List<Map<String, String>>> dayPlans, String mappedRegion, String duration) {
        String accommodationCategory = categoryMap.get("숙박");
        List<Map<String, String>> accommodations = fetchEventsForCategory(mappedRegion, accommodationCategory);

        // 첫째 날 숙박 추가
        if (!accommodations.isEmpty() && dayPlans.containsKey("1일차")) {
            Map<String, String> firstNightAccommodation = findNearestAccommodation(dayPlans.get("1일차").get(dayPlans.get("1일차").size() - 1), accommodations);
            dayPlans.get("1일차").add(firstNightAccommodation);
        }

        // 둘째 날 숙박 추가 (2박 3일일 경우에만), 마지막 날 제외
        if (duration.equals("2박 3일") && dayPlans.containsKey("2일차") && !accommodations.isEmpty()) {
            Map<String, String> secondNightAccommodation = findNearestAccommodation(dayPlans.get("2일차").get(dayPlans.get("2일차").size() - 1), accommodations);
            dayPlans.get("2일차").add(secondNightAccommodation);
        }

        // 마지막 날 숙박 데이터 제거 조건
        if (duration.equals("1박 2일") && dayPlans.containsKey("2일차")) {
            dayPlans.get("2일차").removeIf(event -> "숙박".equals(event.get("category")));
        } else if (duration.equals("2박 3일") && dayPlans.containsKey("3일차")) {
            dayPlans.get("3일차").removeIf(event -> event.get("title").contains("호텔") || "숙박".equals(event.get("category")));
        }
    }

    // 좌표 간 거리 계산 (단위: km)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구 반지름 (단위: km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon1 - lon2);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // 거리 기준으로 이벤트 필터링
    private List<Map<String, String>> filterEventsByDistance(List<Map<String, String>> events, double baseLat, double baseLon, double maxDistance) {
        return events.stream()
                .filter(event -> {
                    double eventLat = Double.parseDouble(event.get("mapx"));
                    double eventLon = Double.parseDouble(event.get("mapy"));
                    return calculateDistance(baseLat, baseLon, eventLat, eventLon) <= maxDistance;
                })
                .collect(Collectors.toList());
    }

    // 가까운 거리로 이벤트를 그룹화하는 메서드
    private List<Map<String, String>> groupEventsByProximity(List<Map<String, String>> events, double maxDistance) {
        List<Map<String, String>> groupedEvents = new ArrayList<>();
        Set<Map<String, String>> processed = new HashSet<>();

        for (Map<String, String> event : events) {
            if (processed.contains(event)) continue;

            double baseLat = Double.parseDouble(event.get("mapx"));
            double baseLon = Double.parseDouble(event.get("mapy"));
            List<Map<String, String>> closeEvents = new ArrayList<>();

            for (Map<String, String> targetEvent : events) {
                if (!processed.contains(targetEvent)) {
                    double targetLat = Double.parseDouble(targetEvent.get("mapx"));
                    double targetLon = Double.parseDouble(targetEvent.get("mapy"));
                    double distance = calculateDistance(baseLat, baseLon, targetLat, targetLon);

                    if (distance <= maxDistance) {
                        closeEvents.add(targetEvent);
                        processed.add(targetEvent);
                    }
                }
            }
            groupedEvents.addAll(closeEvents);
        }

        return groupedEvents;
    }

    // 카테고리에 따른 이벤트를 조회하는 메서드
    private List<Map<String, String>> fetchEventsForCategory(String region, String mappedCategory) {
        List<Map<String, String>> eventList = new ArrayList<>();

        // 각 카테고리에 맞는 데이터를 조회하고 mapx, mapy를 포함해 변환
        List<FoodEvent> foodEvents = foodEventRepository.findByAddr1ContainingAndContenttypeid(region, mappedCategory);
        List<TourEvent> tourEvents = tourEventRepository.findByAddr1ContainingAndContenttypeid(region, mappedCategory);
        List<CulturalFacility> culturalFacilities = culturalFacilityRepository.findByAddr1ContainingAndContenttypeid(region, mappedCategory);
        List<ShoppingEvent> shoppingEvents = shoppingEventRepository.findByAddr1ContainingAndContenttypeid(region, mappedCategory);
        List<TouristAttraction> touristAttractions = touristAttractionRepository.findByAddr1ContainingAndContenttypeid(region, mappedCategory);
        List<LeisureSportsEvent> leisureSportsEvents = leisureSportsEventRepository.findByAddr1ContainingAndContenttypeid(region, mappedCategory);
        List<LocalEvent> localEvents = localEventRepository.findByAddr1ContainingAndContenttypeid(region, mappedCategory);

        // 각 이벤트를 맵으로 변환하여 추가 (mapx, mapy 포함)
        foodEvents.forEach(event -> eventList.add(createEventMap(event.getTitle(), event.getFirstimage(), event.getAddr1(), event.getMapx(), event.getMapy(), event.getContentid(), event.getContenttypeid())));
        tourEvents.forEach(event -> eventList.add(createEventMap(event.getTitle(), event.getFirstimage(), event.getAddr1(), event.getMapx(), event.getMapy(), event.getContentid(), event.getContenttypeid())));
        culturalFacilities.forEach(event -> eventList.add(createEventMap(event.getTitle(), event.getFirstimage(), event.getAddr1(), event.getMapx(), event.getMapy(), event.getContentid(), event.getContenttypeid())));
        shoppingEvents.forEach(event -> eventList.add(createEventMap(event.getTitle(), event.getFirstimage(), event.getAddr1(), event.getMapx(), event.getMapy(), event.getContentid(), event.getContenttypeid())));
        touristAttractions.forEach(event -> eventList.add(createEventMap(event.getTitle(), event.getFirstimage(), event.getAddr1(), event.getMapx(), event.getMapy(), event.getContentid(), event.getContenttypeid())));
        localEvents.forEach(event -> eventList.add(createEventMap(event.getTitle(), event.getFirstimage(), event.getAddr1(), event.getMapx(), event.getMapy(), event.getContentid(), event.getContenttypeid())));
        //leisureSportsEvents.forEach(event -> eventList.add(createEventMap(event.getTitle(), event.getFirstimage(), event.getAddr1(), event.getMapx(), event.getMapy())));

        return eventList;
    }
    // 가장 가까운 숙박 시설 찾기
    private Map<String, String> findNearestAccommodation(Map<String, String> lastEvent, List<Map<String, String>> accommodations) {
        double lastLat = Double.parseDouble(lastEvent.get("mapx"));
        double lastLon = Double.parseDouble(lastEvent.get("mapy"));

        return accommodations.stream()
                .min(Comparator.comparingDouble(accommodation -> {
                    double accLat = Double.parseDouble(accommodation.get("mapx"));
                    double accLon = Double.parseDouble(accommodation.get("mapy"));
                    return calculateDistance(lastLat, lastLon, accLat, accLon);
                }))
                .orElse(accommodations.get(0));
    }

    // 여행 기간에 따라 최대 이벤트 개수 결정
    private int getMaxEventsBasedOnDuration(String duration) {
        switch (duration) {
            case "당일":
                return 5; // 당일 여행의 경우 5개 이벤트 추천
            case "1박 2일":
                return 7; // 1박 2일은 최대 7개 추천
            case "2박 3일":
                return 10; // 2박 3일의 경우 10개 이벤트 추천
            default:
                return 3;
        }
    }

    // 이벤트 데이터를 맵으로 변환하는 유틸리티 메서드
    private Map<String, String> createEventMap(String title, String imageUrl, String addr1, String mapx, String mapy, String contentid, String contenttypeid) {
        Map<String, String> eventMap = new HashMap<>();
        eventMap.put("title", title);
        eventMap.put("image", imageUrl != null ? imageUrl : "이미지 없음");
        eventMap.put("addr1", addr1);
        eventMap.put("mapx", mapx != null ? mapx : "0");
        eventMap.put("mapy", mapy != null ? mapy : "0");
        eventMap.put("contentid", contentid);
        eventMap.put("contenttypeid", contenttypeid);
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
        requestBody.put("max_tokens", 1000);
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