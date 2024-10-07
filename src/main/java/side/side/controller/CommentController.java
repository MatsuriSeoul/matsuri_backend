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
import side.side.model.TourEvent;
import side.side.model.UserInfo;
import side.side.service.CommentService;
import side.side.service.NoticeService;
import side.side.service.TourEventService;
import side.side.service.UserService;

import java.util.List;
import java.util.Map;
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

    @Autowired
    private TourEventService tourEventService;

    // 댓글 작성
    @PostMapping
    public ResponseEntity<?> createComment(@RequestBody CommentRequest commentRequest, @RequestHeader("Authorization") String token) {
        try {
            // JWT 토큰에서 사용자 정보 추출
            Long userId = jwtUtils.extractUserId(token);

            // 사용자 정보 조회
            UserInfo user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 공지사항 정보 조회
//            Notice notice = noticeService.getNoticeById(commentRequest.getNoticeId())
//                    .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));

//            TourEvent tourEvent = tourEventService.findBycontentid(commentRequest.getContentid())
//                    .orElseThrow(() -> new RuntimeException("해당 콘텐츠 ID에 대한 이벤트를 찾을 수 없습니다."));

            // 이름 마스킹 처리 (예: 홍길동 -> 홍OO)
            String maskedName = maskName(user.getUserName());

            // 댓글 생성 및 저장
            Comment comment = new Comment();
            comment.setContent(commentRequest.getContent());
//            comment.setNotice(notice);
//            comment.setContentid(tourEvent);  // 조회된 TourEvent 객체 설정
            comment.setAuthor(user);  // 댓글 작성자 정보 저장
            comment.setMaskedAuthor(maskedName);

            // 공지사항 댓글 작성
            if (commentRequest.getNoticeId() != null) {
                Notice notice = noticeService.getNoticeById(commentRequest.getNoticeId())
                        .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
                comment.setNotice(notice);
            }

            // 행사 댓글 작성
            if (commentRequest.getContentid() != null) {
                TourEvent tourEvent = tourEventService.findBycontentid(commentRequest.getContentid())
                        .orElseThrow(() -> new RuntimeException("해당 콘텐츠 ID에 대한 이벤트를 찾을 수 없습니다."));
                comment.setContentid(tourEvent);
            }

            commentService.createComment(comment);

            return ResponseEntity.ok("댓글이 작성되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 작성 실패");
        }
    }

    // 이름 마스킹
    private String maskName(String name) {
        if (name == null || name.length() < 2) {
            return name;  // 이름이 너무 짧거나 없는 경우 그대로 반환
        }

        // 성 뒤에 모든 글자를 O로 마스킹 처리 (홍길동 -> 홍OO)
        return name.charAt(0) + "O".repeat(name.length() - 1);
    }


    // 댓글 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updateComment(@PathVariable Long id, @RequestBody Map<String, String> payload, @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtils.extractUserId(token);  // JWT에서 사용자 ID 추출
            String newContent = payload.get("content");

            // 작성자 확인 및 댓글 수정
            Comment updatedComment = commentService.updateComment(id, newContent, userId);
            return ResponseEntity.ok(updatedComment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 수정 실패");
        }
    }

    // 댓글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtils.extractUserId(token);  // JWT에서 사용자 ID 추출
            commentService.deleteComment(id, userId);  // 작성자 확인 후 삭제
            return ResponseEntity.ok("댓글이 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 삭제 실패");
        }
    }


    // 특정 공지사항의 댓글 가져오기
    @GetMapping("/notice/{noticeId}")
    public List<Comment> getCommentByNoticeId(@PathVariable Long noticeId) {
        return commentService.getCommentByNoticeId(noticeId);
    }

    @GetMapping("/events/{contentid}")
    public List<Comment> getCommentByEventId(@PathVariable TourEvent contentid){
        return commentService.getCommentByEventId(contentid);
    }



    @Getter
    @Setter
    public static class CommentRequest {
        private String content;
        private Long noticeId;
        private String contentid;
    }
}
