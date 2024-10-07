package side.side.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import side.side.model.Comment;
import side.side.model.TourEvent;
import side.side.repository.CommentRepository;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public Comment createComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public List<Comment> getCommentByNoticeId(Long noticeId) {
        return commentRepository.findByNoticeId(noticeId);
    }

    public Comment updateComment(Long commentId, String content, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 댓글 작성자 확인
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        comment.setContent(content);
        return commentRepository.save(comment);
    }

    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 댓글 작성자 확인
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }

    public List<Comment> getCommentByEventId(TourEvent contentid) {
        return commentRepository.findBycontentid(contentid);
    }
}
