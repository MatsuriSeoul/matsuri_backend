package side.side.controller;

import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;  // BCrypt를 사용한 비밀번호 비교
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import side.side.config.JwtUtils;
import side.side.model.UserInfo;
import side.side.repository.UserRepository;
import side.side.response.LoginResponse;

@RestController
@Slf4j
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody UserInfo userinfo) {
        // 사용자 ID를 기반으로 사용자 검색
        UserInfo storedUser = userRepository.findByUserId(userinfo.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 사용자의 ID를 찾을 수 없습니다: ID " + userinfo.getUserId()));

        // 제출된 비밀번호를 암호화된 비밀번호와 비교
        if (!BCrypt.checkpw(userinfo.getUserPassword(), storedUser.getUserPassword())) {
            // 비밀번호가 일치하지 않으면 로그를 남기고 에러 응답 반환
            logger.warn("비밀번호 불일치: ID {}", userinfo.getUserId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호가 일치하면 JWT 토큰 생성
        String token = jwtUtils.generateToken(storedUser.getUserName(), storedUser.getId(), storedUser.getRole());

        // 로그인 성공 로그 출력 (로그 레벨을 debug로 설정)
        logger.debug("로그인 성공: ID {}", storedUser.getId());

        // 토큰과 사용자 정보를 응답
        return ResponseEntity.ok(new LoginResponse(token, storedUser.getUserName(), storedUser.getUserId()));
    }
}
