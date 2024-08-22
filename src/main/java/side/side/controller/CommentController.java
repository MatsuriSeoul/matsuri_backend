package side.side.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.config.JwtUtils;
import side.side.model.Comment;
import side.side.model.Notice;
import side.side.model.UserInfo;
import side.side.service.CommentService;
import side.side.service.NoticeService;
import side.side.service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    // 댓글 작성
    @PostMapping
    public ResponseEntity<?> createComment(@RequestBody CommentRequest commentRequest, @RequestHeader("Authorization") String token) {
        try {
            // JWT 토큰에서 사용자 정보 추출
            Long userId = jwtUtils.extractUserId(token);

            // 사용자 정보 조회
            UserInfo user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("사용자를 찾을 수 없습니다.");
            }

            // 댓글 생성 및 저장
            Comment comment = new Comment();
            comment.setContent(commentRequest.getContent());
            comment.setNotice(noticeService.getNoticeById(commentRequest.getNoticeId()).orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다")));
            comment.setAuthor(maskName(user.getUserName()));  // 이름 마스킹

            commentService.createComment(comment);

            return ResponseEntity.ok("댓글이 작성되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 작성 실패");
        }
    }

    private String maskName(String name) {
        if (name == null || name.length() < 2) return name;
        return name.charAt(0) + "O".repeat(name.length() - 1);
    }

    // 특정 공지사항의 댓글 가져오기
    @GetMapping("/notice/{noticeId}")
    public List<Comment> getCommentByNoticeId(@PathVariable Long noticeId) {
        return commentService.getCommentByNoticeId(noticeId);
    }



    @Getter
    @Setter
    public static class CommentRequest {
        private String content;
        private Long noticeId;
    }
}
