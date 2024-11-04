package side.side.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.service.OpenAIService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/openai")
public class OpenAIController {
    private static final Logger logger = LoggerFactory.getLogger(OpenAIController.class);
    private final OpenAIService openAIService;

    public OpenAIController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    // 지역과 카테고리를 기반으로 OpenAI API 호출
    @PostMapping("/prompt")
    public ResponseEntity<Map<String, Object>> getOpenAIResponse(@RequestBody Map<String, String> request) {
        String region = request.get("region");
        String category = request.get("category");

        // 로그 추가 (클라이언트에서 받은 지역 정보)
        logger.info("요청된 지역: {}", region);
        logger.info("요청된 카테고리: {}", category != null ? category : "카테고리 없음");

        return openAIService.getResponse(region, category);
    }

    // POST 요청을 처리하는 메소드
    @PostMapping("/recommendation")
    public ResponseEntity<Map<String, String>> getRecommendation(@RequestBody Map<String, String> requestBody) {
        // requestBody에서 title 가져오기
        String title = requestBody.get("title");

        // OpenAI 서비스 호출하여 추천 문구 생성
        String recommendation = openAIService.getRecommendationForTitle(title);

        // 결과를 Map으로 리턴
        return ResponseEntity.ok(Map.of("recommendation", recommendation));
    }

    // 사용자별 맞춤 추천 데이터 제공하는 API
    @GetMapping("/personalized-recommendation/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getPersonalizedRecommendations(@PathVariable Long userId) {
        List<Map<String, Object>> recommendations = openAIService.getPersonalizedRecommendations(userId);
        if (recommendations.isEmpty()) {
            return ResponseEntity.ok(List.of(Map.of("message", "추천할 데이터가 없습니다.")));
        }
        return ResponseEntity.ok(recommendations);
    }


    // AI 여행 계획 플래너 지역 선택 처리
    @PostMapping("/region")
    public ResponseEntity<Map<String, String>> getRegionResponse(@RequestBody Map<String, String> request) {
        String region = request.get("region");

        // 로그 추가 (클라이언트에서 받은 지역 정보)
        logger.info("요청된 지역: {}", region);

        // 지역만 처리하는 로직 추가 (카테고리 없음)
        return ResponseEntity.ok(Map.of("message", "다음으로 카테고리를 선택해 주세요!"));
    }

    // 지역과 카테고리를 기반으로 여행 계획을 처리하는 메서드
    @PostMapping("/plan")
    public ResponseEntity<Map<String, Object>> getPlanResponse(@RequestBody Map<String, Object> request) {
        String region = (String) request.get("region");
        List<String> categories = (List<String>) request.get("categories");

        // 로그 추가 (클라이언트에서 받은 지역 및 카테고리 정보)
        logger.info("요청된 지역: {}", region);
        logger.info("요청된 카테고리: {}", categories);

        // 이 단계에서는 duration을 기다리지 않고, 먼저 카테고리만 준비해둠
        return ResponseEntity.ok(Map.of("message", "카테고리 선택 완료, 다음 단계로 이동하세요."));
    }

    // 지역, 카테고리, 기간을 받아서 AI가 여행 계획을 생성
    @PostMapping("/result")
    public ResponseEntity<Map<String, Object>> getAIPlanResult(@RequestBody Map<String, Object> request) {
        String region = (String) request.get("region");
        List<String> categories = (List<String>) request.get("categories"); // 다중 카테고리
        String duration = (String) request.get("duration");

        // OpenAIService를 호출하여 결과 생성
        Map<String, Object> aiPlanResult = openAIService.generateAIPlan(region, categories, duration, true);

        return ResponseEntity.ok(aiPlanResult);
    }

    @PostMapping("/refresh-plan")
    public ResponseEntity<Map<String, Object>> refreshAIPlan(@RequestBody Map<String, Object> request) {
        String region = (String) request.get("region");
        List<String> categories = (List<String>) request.get("categories"); // category 배열로 받아 처리
        String duration = (String) request.get("duration");

        // OpenAIService를 호출하여 새로운 여행 계획 생성
        Map<String, Object> refreshedPlan = openAIService.generateAIPlan(region, categories, duration, true);

        return ResponseEntity.ok(refreshedPlan);
    }
}