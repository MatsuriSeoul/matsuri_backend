package side.side.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.config.JwtUtils;
import side.side.model.UserInfo;
import side.side.repository.UserRepository;
import side.side.response.LoginResponse;
import side.side.service.UserService;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/save")
    public ResponseEntity<String> saveUser(@RequestBody UserInfo userinfo) {
        if (userRepository.existsByUserId(userinfo.getUserId())) {
            // 사용자 ID가 이미 존재하면
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 존재하는 아이디 입니다.");
        }
        if (userService.checkUserEmailExists(userinfo.getUserEmail()) || userService.checkUserPhoneExists(userinfo.getUserPhone())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이메일 또는 휴대폰 번호가 이미 사용 중입니다.");
        }
        try {
            userService.saveUser(userinfo); // 사용자 정보 저장
            // IDE 로그 출력
            log.info("회원가입 성공 userID: {}", userinfo.getUserId());
            return ResponseEntity.ok().body("환영합니다 ~!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 저장 오류 : " + e.getMessage());
        }
    }

    // 회원가입시 사용자 role을 'USER'로 설정하기 위한 경로
    @PostMapping("/register")
    public UserInfo registerUser(@RequestBody UserInfo userInfo) {
        return userService.saveUser(userInfo);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserInfo loginRequest) {
        UserInfo user = userService.findByUserId(loginRequest.getUserId());
        if (user != null && loginRequest.getUserPassword().equals(user.getUserPassword())) { // 평문 비교
            String token = jwtUtils.generateToken(user.getUserName(), user.getId(), user.getRole());
            return ResponseEntity.ok(new LoginResponse(token, user.getUserName(), user.getRole()));
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @GetMapping("/check-id/{userId}")
    public ResponseEntity<?> checkUserId(@PathVariable String userId) {
        boolean exists = userService.checkUserIdExists(userId);
        return ResponseEntity.ok().body(new CheckResponse(exists));
    }

    @GetMapping("/check-email/{userEmail}")
    public ResponseEntity<?> checkUserEmail(@PathVariable String userEmail) {
        boolean exists = userService.checkUserEmailExists(userEmail);
        return ResponseEntity.ok().body(new CheckResponse(exists));
    }

    @GetMapping("/check-phone/{userPhone}")
    public ResponseEntity<CheckResponse> checkUserPhone(@PathVariable String userPhone) {
        boolean exists = userService.checkUserPhoneExists(userPhone);
        return ResponseEntity.ok(new CheckResponse(exists));
    }

    @PostMapping("/send-verification-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody VerificationRequest request) {
        boolean isSent = false;
        if ("email".equals(request.getType())) {
            isSent = userService.sendVerificationCodeByEmail(request.getIdentifier());
        } else if ("phone".equals(request.getType())) {
            isSent = userService.sendVerificationCodeByPhone(request.getIdentifier());
        }

        if (isSent) {
            return ResponseEntity.ok("인증번호가 발송되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("인증번호 발송 실패");
        }
    }

    @PostMapping("/verify-code")
    public ResponseEntity<VerifyResponse> verifyCode(@RequestBody VerificationRequest request) {
        boolean verified = userService.verifyCode(request.getIdentifier(), request.getCode());
        return ResponseEntity.ok(new VerifyResponse(verified));
    }

    @Getter
    @Setter
    public static class VerificationRequest {
        private String identifier;  // 이메일 또는 전화번호
        private String type;         // "email" 또는 "phone"
        private String code;         // 인증번호 (검증할 때 사용)
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class VerifyResponse {
        private boolean verified;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class CheckResponse {
        private boolean exists;
    }


    //  로그인 한 사용자의 정보를 반환하는 메소드
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            // JWT 토큰에서 사용자 ID 추출
            Long userId = jwtUtils.extractUserId(token);

            // 사용자 정보 조회
            UserInfo user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 정보를 가져오지 못했습니다.");
        }
    }
}

