package side.side.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.config.JwtUtils;
import side.side.model.DTO.LikeRequestDTO;
import side.side.model.DTO.UserDTO;
import side.side.model.UserInfo;
import side.side.service.LikeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<String> likeContent(
            @RequestHeader("Authorization") String token,
            @RequestParam String contentId,
            @RequestParam String contentType) {

        Long userId = jwtUtils.extractUserId(token);
        likeService.likeContent(userId, contentId, contentType);
        return ResponseEntity.ok("좋아요가 추가되었습니다.");
    }

    // 좋아요 취소
    @DeleteMapping()
    public ResponseEntity<String> unlikeContent(
            @RequestHeader("Authorization") String token,
            @RequestParam String contentId,
            @RequestParam String contentType) {

        Long userId = jwtUtils.extractUserId(token);
        likeService.unlikeContent(userId, contentId, contentType);
        return ResponseEntity.ok("좋아요가 취소되었습니다.");
    }

    //  좋아요 수 조회
    @GetMapping("/count")
    public ResponseEntity<Integer> getLikeCount(
            @RequestParam String contentId,
            @RequestParam String contentType) {

        int likeCount = likeService.getLikeCount(contentId, contentType);
        return ResponseEntity.ok(likeCount);
    }

    // 특정 콘텐츠의 좋아요 상태 및 숫자 조회
    @GetMapping("/{contentId}")
    public ResponseEntity<?> getLikeStatus(@PathVariable String contentId,@RequestParam String contentType ,@RequestHeader("Authorization") String token) {
        try {
            // JWT 토큰에서 사용자 ID 추출
            Long userId = jwtUtils.extractUserIdFromToken(token);

            // 사용자가 좋아요를 눌렀는지 확인
            boolean isLiked = likeService.isLiked(userId, contentId);

            // 특정 콘텐츠에 대한 좋아요 숫자
            int likeCount = likeService.getLikeCount(contentId, contentType);

            // 응답으로 좋아요 상태와 좋아요 숫자를 반환
            Map<String, Object> response = new HashMap<>();
            response.put("isLiked", isLiked);
            response.put("likeCount", likeCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("서버에서 오류가 발생했습니다.");
        }

    }


    // 특정 콘텐츠에 좋아요를 누른 사용자 목록 조회
    @GetMapping("/{contentId}/users")
    public ResponseEntity<?> getLikedUsers(@PathVariable String contentId) {
        try {
            List<UserDTO> likedUsers = likeService.getLikedUsers(contentId);
            return ResponseEntity.ok(likedUsers);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("서버에서 오류가 발생했습니다.");
        }
    }
}
