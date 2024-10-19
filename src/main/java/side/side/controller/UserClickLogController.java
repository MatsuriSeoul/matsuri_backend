package side.side.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.config.JwtUtils;
import side.side.model.UserInfo;
import side.side.service.UserClickLogService;

@RestController
@RequestMapping("/api/clicks")
public class UserClickLogController {

    @Autowired
    private UserClickLogService userClickLogService;

    @Autowired
    private JwtUtils jwtUtils;


    @PostMapping("/log")
    public ResponseEntity<String> logUserClick(
            @RequestHeader("Authorization") String token, // JWT 토큰을 요청 헤더에서 받음
            @RequestBody ClickLogRequest request) {

        // JWT 토큰에서 사용자 정보 추출
        Long userId = jwtUtils.extractUserId(token);  // JWT에서 userId 추출
        if (userId == null) {
            return ResponseEntity.status(401).body("토큰이 유효하지 않아요");
        }

        // 클릭 로그 저장
        userClickLogService.logUserClick(userId, request.getContentid(), request.getCategory());

        return ResponseEntity.ok("로그를 남기는데 성공함");
    }



    @Getter
    @Setter
    public static class ClickLogRequest {
        private String contentid;  // 콘텐츠 ID
        private String category;   // 카테고리 이름

    }
}
