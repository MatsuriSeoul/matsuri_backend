package side.side.controller;

import org.springframework.web.bind.annotation.*;
import side.side.service.OpenAIService;

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
    public String getOpenAIResponse(@RequestBody Map<String, String> request) {
        String region = request.get("region");
        String category = request.get("category");
        return openAIService.getResponseFromOpenAI(region, category);
    }
}
