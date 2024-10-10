package side.side.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")  // application.properties에서 API 키를 불러옴
    private String openaiApiKey;

    private final RestTemplate restTemplate;

    public OpenAIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getResponseFromOpenAI(String prompt) {
        String url = "https://api.openai.com/v1/chat/completions";  // OpenAI API 엔드포인트

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openaiApiKey);
        headers.set("Content-Type", "application/json");

        // 요청 본문 설정
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");  // OpenAI 모델
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)  // 'prompt' 대신 'messages' 필드 사용
        ));
        requestBody.put("max_tokens", 100);  // 응답 길이 설정
        requestBody.put("temperature", 0.7);  // 창의성 정도

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // OpenAI API 호출
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return response.getBody();  // 응답 본문 반환
    }
}
