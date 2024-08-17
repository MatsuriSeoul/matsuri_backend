package side.side.controller;

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
            return ResponseEntity.status(HttpStatus.CONFLICT).body("유저 아이디 이미 존재");
        }
        try {
            userService.saveUser(userinfo); // 사용자 정보 저장
            // IDE 로그 출력
            String successMessage = "회원가입 성공 userID: " + userinfo.getUserId();
            logger.info(successMessage);
            return ResponseEntity.ok().body(successMessage);
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
            String token = jwtUtils.generateToken(user.getUserName(), user.getId());
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

    // CheckResponse 클래스 정의
    @Setter
    @Getter
    public static class CheckResponse {
        private boolean exists;

        public CheckResponse(boolean exists) {
            this.exists = exists;
        }

    }
}
