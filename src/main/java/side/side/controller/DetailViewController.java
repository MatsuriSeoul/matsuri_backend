package side.side.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.config.JwtUtils;
import side.side.service.DetailViewService;


import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;

@RestController
@RequestMapping("/api/detail-view")
public class DetailViewController {

    @Autowired
    private DetailViewService detailViewService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/{contenttypeid}/{contentid}")
    public ResponseEntity<?> addDetailView(@PathVariable String contenttypeid,
                                           @PathVariable String contentid,
                                           HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        token = token.substring(7); // "Bearer " 이후의 토큰 값만 추출
        Long userId;
        try {
            userId = jwtUtils.extractUserId(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        // 조회수 증가 로직 호출
        boolean isViewAdded = detailViewService.addDetailView(userId, contenttypeid, contentid);

        if (isViewAdded) {
            return ResponseEntity.ok("조회수가 증가했습니다.");
        } else {
            return ResponseEntity.ok("이미 오늘 조회한 내용입니다.");
        }
    }


    // 조회수를 가져오는 메소드 추가
    @GetMapping("/{contenttypeid}/{contentid}/count")
    public ResponseEntity<?> getDetailViewCount(
            @PathVariable String contenttypeid,
            @PathVariable String contentid) {
        try {
            int viewCount = detailViewService.getViewCount(contenttypeid, contentid);
            return ResponseEntity.ok(Collections.singletonMap("viewCount", viewCount));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("조회수 가져오기 실패: " + e.getMessage());
        }
    }
}

