package side.side.service;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    @Autowired
    private TourEventRepository tourEventRepository;

    @Autowired
    private TouristAttractionRepository touristAttractionRepository;

    @Autowired
    private FoodEventRepository foodEventRepository;

    @Autowired
    private LocalEventRepository localEventRepository;

    @Autowired
    private ShoppingEventRepository shoppingEventRepository;

    @Autowired
    private TravelCourseRepository travelCourseRepository;

    @Autowired
    private LeisureSportsEventRepository leisureSportsEventRepository;

    @Autowired
    private CulturalFacilityRepository culturalFacilityRepository;

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
        // 지역과 카테고리를 매핑된 값으로 변환
        String mappedRegion = regionMap.getOrDefault(region, null);
        String mappedCategory = categoryMap.getOrDefault(category, null);

        // 매핑된 값 확인
        System.out.println("매핑된 지역: " + mappedRegion);
        System.out.println("매핑된 카테고리: " + mappedCategory);

        if (mappedRegion == null || mappedCategory == null) {
            return "잘못된 지역 또는 카테고리입니다. 다시 선택해 주세요.";
        }

        // 지역에 따라 검색할 문자열 변환 (예: '경남' -> '경상남도')
        String queryRegion = switch (mappedRegion) {
            case "서울" -> "서울특별시";
            case "경남" -> "경상남도";
            case "경북" -> "경상북도";
            case "전남" -> "전라남도";
            case "충남" -> "충청남도";
            case "충북" -> "충청북도";
            default -> mappedRegion;
        };

        // DB에서 지역과 카테고리에 맞는 데이터를 조회 (카테고리별로 분리된 로직)
        StringBuilder eventList = new StringBuilder();
        List<String> titles = null;

        // 관광지("12") 관련 데이터 처리
        if ("12".equals(mappedCategory)) {
            List<TouristAttraction> attractions = touristAttractionRepository.findByAddr1ContainingAndContenttypeid(queryRegion, mappedCategory);
            titles = attractions.stream().map(TouristAttraction::getTitle).toList();
        }

        // 문화시설("14") 관련 데이터 처리
        else if ("14".equals(mappedCategory)) {
            List<CulturalFacility> facilities = culturalFacilityRepository.findByAddr1ContainingAndContenttypeid(queryRegion, mappedCategory);
            titles = facilities.stream().map(CulturalFacility::getTitle).toList();
        }

        // 행사("15") 관련 데이터 처리
        else if ("15".equals(mappedCategory)) {
            List<TourEvent> events = tourEventRepository.findByAddr1ContainingAndContenttypeid(queryRegion, mappedCategory);
            titles = events.stream().map(TourEvent::getTitle).toList();
        }

        // 여행코스("25") 관련 데이터 처리
        else if ("25".equals(mappedCategory)) {
            List<TravelCourse> courses = travelCourseRepository.findByAddr1ContainingAndContenttypeid(queryRegion, mappedCategory);
            titles = courses.stream().map(TravelCourse::getTitle).toList();
        }

        // 레포츠("28") 관련 데이터 처리
        else if ("28".equals(mappedCategory)) {
            List<LeisureSportsEvent> sports = leisureSportsEventRepository.findByAddr1ContainingAndContenttypeid(queryRegion, mappedCategory);
            titles = sports.stream().map(LeisureSportsEvent::getTitle).toList();
        }

        // 숙박("32") 관련 데이터 처리
        else if ("32".equals(mappedCategory)) {
            List<LocalEvent> accommodations = localEventRepository.findByAddr1ContainingAndContenttypeid(queryRegion, mappedCategory);
            titles = accommodations.stream().map(LocalEvent::getTitle).toList();
        }

        // 쇼핑("38") 관련 데이터 처리
        else if ("38".equals(mappedCategory)) {
            List<ShoppingEvent> shopping = shoppingEventRepository.findByAddr1ContainingAndContenttypeid(queryRegion, mappedCategory);
            titles = shopping.stream().map(ShoppingEvent::getTitle).toList();
        }

        // 음식("39") 관련 데이터 처리
        else if ("39".equals(mappedCategory)) {
            List<FoodEvent> foods = foodEventRepository.findByAddr1ContainingAndContenttypeid(queryRegion, mappedCategory);
            titles = foods.stream().map(FoodEvent::getTitle).toList();
        }

        // OpenAI API를 사용하여 추천 문구 생성
        if (titles != null && !titles.isEmpty()) {
            for (String title : titles) {
                eventList.append(generateRecommendation(title)).append("\n");
            }
        }

        // OpenAI API 호출 준비
        String prompt = "다음은 " + region + "에서 열리는 행사 목록입니다. 이 목록을 잘 포맷된 정보로 요약해 주세요:\n\n" + eventList.toString();

        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openaiApiKey);
        headers.set("Content-Type", "application/json");

        // GPT-4 모델 사용
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", prompt)
        ));
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return response.getBody();
    }

    // OpenAI API를 통해 추천 문구를 생성하는 메서드
    private String generateRecommendation(String title) {
        // OpenAI에게 요청할 추천 문구 생성 요청
        String prompt = "행사 제목 '" + title + "'을 기반으로 매력적인 추천 문구를 생성해 주세요.";

        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openaiApiKey);
        headers.set("Content-Type", "application/json");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", prompt)
        ));
        requestBody.put("max_tokens", 100);
        requestBody.put("temperature", 0.7); // 창의적인 응답을 유도하기 위한 설정

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        // 결과를 반환
        return response.getBody();
    }
}
