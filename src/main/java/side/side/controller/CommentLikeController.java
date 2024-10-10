package side.side.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.config.JwtUtils;
import side.side.service.LikeService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/comment-likes")
public class CommentLikeController {
    @Autowired
    private LikeService likeService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/{commentId}")
    public ResponseEntity<String> likeComment(
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtils.extractUserIdFromToken(token);
        likeService.likeComment(userId, commentId);
        return ResponseEntity.ok("댓글에 좋아요가 추가되었습니다.");
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> unlikeComment(
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtils.extractUserIdFromToken(token);
        likeService.unlikeComment(userId, commentId);
        return ResponseEntity.ok("댓글에 대한 좋아요가 취소되었습니다.");
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<?> getCommentLikeStatus(
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtils.extractUserIdFromToken(token);
        boolean isLiked = likeService.isCommentLiked(userId, commentId);
        int likeCount = likeService.getCommentLikeCount(commentId);
        Map<String, Object> response = new HashMap<>();
        response.put("isLiked", isLiked);
        response.put("likeCount", likeCount);
        return ResponseEntity.ok(response);
    }
}

