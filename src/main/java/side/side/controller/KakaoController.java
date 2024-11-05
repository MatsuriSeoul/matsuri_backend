package side.side.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
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

@Slf4j
@RestController
public class KakaoController {


    private final String kakaoClientId;


    private final String kakaoRedirectUri;


    private final String kakaoClientSecret; // 클라이언트 시크릿을 불러오기 위한 필드 추가

    public KakaoController() {
        Dotenv dotenv = Dotenv.load();
        kakaoClientId = dotenv.get("KAKAO_CLIENT_ID");
        kakaoRedirectUri = dotenv.get("KAKAO_REDIRECT_URI");
        kakaoClientSecret = dotenv.get("KAKAO_CLIENT_SECRET");
    }

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @GetMapping("/api/kakao/callback")
    public ResponseEntity<?> kakaoCallback(@RequestParam("code") String code) {
        try {
            // 액세스 토큰 요청
            String tokenUrl = "https://kauth.kakao.com/oauth/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 클라이언트 시크릿 추가
            String body = "grant_type=authorization_code"
                    + "&client_id=" + kakaoClientId
                    + "&redirect_uri=" + kakaoRedirectUri
                    + "&code=" + code
                    + "&client_secret=" + kakaoClientSecret; // 클라이언트 시크릿 포함

            HttpEntity<String> request = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();

            // 토큰 요청 및 응답 처리
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);
            System.out.println("Token Response: " + response.getBody());

            // 액세스 토큰 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String accessToken = jsonNode.get("access_token").asText();
            System.out.println("Access Token: " + accessToken);

            // 사용자 정보 요청
            String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.setBearerAuth(accessToken);
            HttpEntity<String> userInfoRequest = new HttpEntity<>(userInfoHeaders);

            ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                    userInfoUrl, HttpMethod.GET, userInfoRequest, String.class);

            JsonNode userInfo = objectMapper.readTree(userInfoResponse.getBody());
            System.out.println("User Info: " + userInfo);

            String socialId = userInfo.get("id").asText();
            String userName = userInfo.get("properties").get("nickname").asText();
//            String userEmail = userInfo.get("kakao_account").get("email").asText(); //개발자센터에서 동의항목 이메일 추가안함

            // 사용자 정보 저장 또는 업데이트
            UserInfo existingUser = userService.findBySocialIdAndProvider(socialId, "kakao");
            UserInfo user;

            if (existingUser == null) {
                user = new UserInfo();
                user.setUserName(userName);
//                user.setUserEmail(userEmail);
                user.setSocialId(socialId);
                user.setSocialProvider("kakao");
                user.setRole("USER");
                userService.saveUser(user);
            } else {
                user = existingUser;
            }

            // JWT 생성 및 클라이언트로 리다이렉트
            String token = jwtUtils.generateToken(user.getUserName(), user.getId(), user.getRole());

            URI redirectUri = new URI("http://localhost:8080/?token=" + token);
            HttpHeaders redirectHeaders = new HttpHeaders();
            redirectHeaders.setLocation(redirectUri);

            return new ResponseEntity<>(redirectHeaders, HttpStatus.FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("카카오 로그인 중 오류 발생");
        }
    }
}

