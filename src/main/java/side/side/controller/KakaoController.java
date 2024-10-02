package side.side.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;
import side.side.config.JwtUtils;
import side.side.model.UserInfo;
import side.side.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.util.Collections;

@Controller
public class KakaoController {

    @Value("${kakao.client.id}")
    private String kakaoClientId;

    @Value("${kakao.redirect.uri}")
    private String kakaoRedirectUri;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @GetMapping("/api/kakao/login")
    public ResponseEntity<?> kakaoLogin() {
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize?response_type=code"
                + "&client_id=" + kakaoClientId
                + "&redirect_uri=" + kakaoRedirectUri;

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(kakaoAuthUrl));
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }

    @GetMapping("/api/login")
    public ResponseEntity<?> kakaoCallback(@RequestParam("code") String code) {
        try {
            // 액세스 토큰 요청
            String tokenUrl = "https://kauth.kakao.com/oauth/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String body = "grant_type=authorization_code"
                    + "&client_id=" + kakaoClientId
                    + "&redirect_uri=" + kakaoRedirectUri
                    + "&code=" + code;

            HttpEntity<String> request = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

            // 액세스 토큰 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String accessToken = jsonNode.get("access_token").asText();

            // 사용자 정보 요청
            String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.setBearerAuth(accessToken);
            HttpEntity<String> userInfoRequest = new HttpEntity<>(userInfoHeaders);

            ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                    userInfoUrl, HttpMethod.GET, userInfoRequest, String.class);

            JsonNode userInfo = objectMapper.readTree(userInfoResponse.getBody());

            String kakaoId = userInfo.get("id").asText();
            String userName = userInfo.get("properties").get("nickname").asText();
            String userEmail = userInfo.get("kakao_account").get("email").asText();

            // 사용자 정보 저장 또는 업데이트
            UserInfo user = userService.findBySocialIdAndProvider(kakaoId, "kakao");

            if (user == null) {
                user = new UserInfo();
                user.setUserName(userName);
                user.setUserEmail(userEmail);
                user.setSocialId(kakaoId);
                user.setSocialProvider("kakao");
                user.setRole("USER");
                userService.saveUser(user);
            }

            // JWT 생성 및 클라이언트로 리다이렉트
            String token = jwtUtils.generateToken(user.getUserName(), user.getId(), user.getRole());
            URI redirectUri = new URI("http://localhost:8080/?token=" + token);
            HttpHeaders redirectHeaders = new HttpHeaders();
            redirectHeaders.setLocation(redirectUri);
            return new ResponseEntity<>(redirectHeaders, HttpStatus.FOUND);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("카카오 로그인 중 오류 발생");
        }
    }
}
