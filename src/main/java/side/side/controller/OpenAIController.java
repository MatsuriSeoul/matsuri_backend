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

    // OpenAI API 호출을 처리하는 엔드포인트
    @PostMapping("/prompt")
    public String getOpenAIResponse(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");  // 프롬프트 파라미터
        return openAIService.getResponseFromOpenAI(prompt);  // OpenAI 서비스 호출
    }
}
