package side.side.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
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
    public ResponseEntity<?> login(@RequestBody UserInfo userinfo){
        // 제공된 사용자 ID를 기반으로 데이터베이스에서 사용자 정보를 검색
        UserInfo storedUser = userRepository.findByUserId(userinfo.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 ID를 찾을수 없습니다 ID :" + userinfo.getUserId()));

        // 제출한 비밀번호와 저장된 비밀번호를 비교
        if(!storedUser.getUserPassword().equals(userinfo.getUserPassword())){
            // 비밀번호가 일치 안 하면 권한 없고 틀림 안내
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호 일치하지 않습니다.");
        }
        // 로그인 하면 JWT 토큰 생성
        String token = jwtUtils.generateToken(storedUser.getUserName(), storedUser.getId().toString());
        // 로그인 로그
        logger.info("로그인 성공 ID : {}", storedUser.getId());

        // 토큰과 사용자 닉네임을 반환
        return ResponseEntity.ok(new LoginResponse(token, storedUser.getUserName(), storedUser.getUserId()));
    }
}
