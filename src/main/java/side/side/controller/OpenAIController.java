package side.side.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.service.OpenAIService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/openai")
public class OpenAIController {

    private final OpenAIService openAIService;

    public OpenAIController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    // 지역과 카테고리를 기반으로 OpenAI API 호출
    @PostMapping("/prompt")
    public ResponseEntity<Map<String, Object>> getOpenAIResponse(@RequestBody Map<String, String> request) {
        String region = request.get("region");
        String category = request.get("category");
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
}
