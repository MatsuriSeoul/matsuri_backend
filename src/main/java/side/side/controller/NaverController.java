package side.side.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import side.side.config.JwtUtils;
import side.side.model.UserInfo;
import side.side.service.UserService;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestController
public class NaverController {

    private static final Logger logger = LoggerFactory.getLogger(NaverController.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    private final String clientId = "cAxVyC6eWpTfHY6rLFwK"; // 네이버 클라이언트 ID
    private final String clientSecret = "MzaDRXyw9H"; // 네이버 클라이언트 시크릿
    private final String redirectUri = "http://localhost:8080/naver/oauth2"; // 리다이렉트 URI

    @GetMapping("/naver/oauth2")
    public ResponseEntity<?> naverCallback(@RequestParam String code, @RequestParam String state) {
        try {
            // 액세스 토큰 요청
            String tokenUrl = "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code" +
                    "&client_id=" + clientId +
                    "&client_secret=" + clientSecret +
                    "&code=" + code +
                    "&state=" + state;

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.postForObject(tokenUrl, null, String.class);

            log.info("네이버로부터 받은 액세스 토큰 응답: {}", response);  // 응답을 로그로 출력

            // JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);

            // 네이버 응답에서 액세스 토큰 추출
            String accessToken = jsonNode.get("access_token").asText();

            log.info("추출된 액세스 토큰: {}", accessToken);  // 액세스 토큰 출력

            // 사용자 정보 요청
            String userInfoUrl = "https://openapi.naver.com/v1/nid/me";
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> userInfoResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity, String.class);

            log.info("네이버 사용자 정보 응답: {}", userInfoResponse.getBody());  // 사용자 정보 응답 로그 출력

            if (userInfoResponse.getStatusCode() == HttpStatus.OK) {
                JsonNode userJson = objectMapper.readTree(userInfoResponse.getBody()).get("response");
                String userName = userJson.get("name").asText();
                String userId = userJson.get("id").asText();
                String userEmail = userJson.has("email") ? userJson.get("email").asText() : null;
                String userPhone = userJson.has("mobile") ? userJson.get("mobile").asText() : null;
                String userBirthday = userJson.has("birthday") ? userJson.get("birthday").asText() : null;

                log.info("사용자 이름: {}, 사용자 ID: {}", userName, userId);  // 사용자 정보 로그 출력

                // DB에서 해당 네이버 사용자 ID가 있는지 확인
                UserInfo existingUser = userService.findBySocialIdAndProvider(userId, "naver");

                if (existingUser == null) {
                    // 새로운 사용자라면 DB에 저장
                    UserInfo newUser = new UserInfo();
                    newUser.setUserName(userName);
                    newUser.setUserEmail(userEmail);
                    newUser.setUserPhone(userPhone);
                    newUser.setUserBirthday(userBirthday);
                    newUser.setSocialId(userId);
                    newUser.setSocialProvider("naver");
                    userService.saveUser(newUser);

                    log.info("새로운 네이버 사용자를 DB에 저장했습니다.");
                } else {
                    log.info("기존 사용자입니다.");
                }

                // JWT 생성
                String token = jwtUtils.generateToken(userName, userId);


                // 사용자 정보와 JWT를 반환
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.add("Set-Cookie", "token=" + token + "; Path=/; HttpOnly;");

                URI redirectUri = URI.create("/");
                return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).headers(responseHeaders).build();

            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("네이버 사용자 정보를 가져오는 데 실패했습니다.");
            }
        } catch (Exception e) {
            log.error("에러 발생: {}", e.getMessage(), e);  // 에러 로그 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("네이버 로그인 처리 중 에러가 발생했습니다.");
        }
    }
}