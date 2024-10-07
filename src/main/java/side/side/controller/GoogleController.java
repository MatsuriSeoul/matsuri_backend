package side.side.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import side.side.config.JwtUtils;
import side.side.model.UserInfo;
import side.side.service.UserService;

import java.net.URI;

@Slf4j
@RestController
public class GoogleController {

    @Value("${google.client.id}")
    private String googleClientId;

    @Value("${google.client.secret}")
    private String googleClientSecret;

    @Value("${google.redirect.uri}")
    private String googleRedirectUri;


    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    // 구글 로그인 URL로 리다이렉션
//    @GetMapping("/api/google/login")
//    public ResponseEntity<?> googleLogin() {
//        String googleAuthUrl = "https://accounts.google.com/o/oauth2/v2/auth?response_type=code"
//                + "&client_id=" + googleClientId
//                + "&redirect_uri=" + googleRedirectUri
//                + "&scope=email profile";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setLocation(URI.create(googleAuthUrl));
//
//        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
//    }

    // 구글에서 Authorization Code를 받아오는 콜백 메서드
    @GetMapping("/login/oauth2/code/google")
    public ResponseEntity<?> googleCallback(@RequestParam("code") String code) {
        try {
            // 1. Authorization Code로 액세스 토큰 요청
            String tokenUrl = "https://oauth2.googleapis.com/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String body = "grant_type=authorization_code"
                    + "&client_id=" + googleClientId
                    + "&client_secret=" + googleClientSecret
                    + "&redirect_uri=" + googleRedirectUri
                    + "&code=" + code;

            HttpEntity<String> request = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

            // 2. 액세스 토큰 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String accessToken = jsonNode.get("access_token").asText();

            // 3. 액세스 토큰으로 구글 사용자 정보 요청
            String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.setBearerAuth(accessToken);
            HttpEntity<String> userInfoRequest = new HttpEntity<>(userInfoHeaders);

            ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                    userInfoUrl, HttpMethod.GET, userInfoRequest, String.class);

            JsonNode userInfo = objectMapper.readTree(userInfoResponse.getBody());
            String socialId = userInfo.get("id").asText();
            String userName = userInfo.get("name").asText();
            String userEmail = userInfo.get("email").asText();

            // 4. 사용자 정보 저장 또는 업데이트
            UserInfo existingUser = userService.findBySocialIdAndProvider(socialId, "google");
            UserInfo user;

            if (existingUser == null) {
                user = new UserInfo();
                user.setUserName(userName);
                user.setUserEmail(userEmail);
                user.setSocialId(socialId);
                user.setSocialProvider("google");
                user.setRole("USER");
                userService.saveUser(user);
            } else {
                user = existingUser;
            }

            // 5. JWT 생성 및 클라이언트로 리다이렉트
            String token = jwtUtils.generateToken(user.getUserName(), user.getId(), user.getRole());

            URI redirectUri = new URI("http://localhost:8080/?token=" + token);
            HttpHeaders redirectHeaders = new HttpHeaders();
            redirectHeaders.setLocation(redirectUri);

            return new ResponseEntity<>(redirectHeaders, HttpStatus.FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("구글 로그인 중 오류 발생");
        }
    }


}
