package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.Comment;
import side.side.model.TourEvent;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByNoticeId(Long NoticeId);
    List<Comment> findBycontentid(String Contentid);

    List<Comment> findBySvcid(String svcid);
    List<Comment> findByGyeonggiEventId(Long gyeonggiEventId);

}
