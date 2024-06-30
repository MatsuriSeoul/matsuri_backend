package side.side.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import side.side.config.JwtUtils;
import side.side.model.UserInfo;
import side.side.repository.UserRepository;
import side.side.service.UserService;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

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
            userRepository.save(userinfo); // 사용자 정보 저장
            // IDE 로그 출력
            String successMessage = "회원가입 성공 userID: " + userinfo.getUserId();
            logger.info(successMessage);
            return ResponseEntity.ok().body(successMessage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 저장 오류 : " + e.getMessage());
        }
    }
}
