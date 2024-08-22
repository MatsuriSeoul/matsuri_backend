package side.side.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import side.side.model.Comment;
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
}
