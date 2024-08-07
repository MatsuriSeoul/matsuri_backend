package side.side.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import side.side.config.JwtUtils;
import side.side.model.Comment;
import side.side.model.Notice;
import side.side.service.CommentService;
import side.side.service.NoticeService;

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
    private JwtUtils jwtUtils;

    @PostMapping
    public Comment createComment(@RequestBody CommentRequest commentRequest) {
        Optional<Notice> notice = noticeService.getNoticeById(commentRequest.noticeId);
        if (notice.isPresent()) {
            Comment comment = new Comment();
            comment.setContent(commentRequest.content);
            comment.setNotice(notice.get());
            comment.setAuthor(commentRequest.getAuthor());
            return commentService.createComment(comment);
        } else {
            throw new RuntimeException("Notice not found");
        }
    }

    @GetMapping("/notice/{noticeId}")
    public List<Comment> getCommentByNoticeId(@PathVariable Long noticeId) {
        return commentService.getCommentByNoticeId(noticeId);
    }

    @Getter
    @Setter
    public static class CommentRequest {
        private String content;
        private Long noticeId;
        private String author;
    }

}