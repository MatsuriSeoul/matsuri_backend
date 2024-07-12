package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByNoticeId(Long NoticeId);
}
