package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.Like;
import side.side.model.UserInfo;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    // 특정 사용자와 콘텐츠에 대한 좋아요 여부 확인
    Optional<Like> findByUserAndContentIdAndContentType(UserInfo user, String contentId, String contentType);

    Optional<Like> findByUserIdAndContentId(Long userId, String contentId);

    // 특정 콘텐츠에 좋아요를 누른 모든 기록을 조회
    List<Like> findByContentId(String contentId);

    // 특정 사용자가 좋아요한 콘텐츠 목록
    List<Like> findByUser(UserInfo user);

    // 특정 게시물에 대한 좋아요 상태 조회
    boolean existsByUserIdAndContentId(Long userId, String contentId);


    int countByContentIdAndContentType(String contentId, String contentType);
}
