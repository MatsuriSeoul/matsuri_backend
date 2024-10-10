package side.side.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import side.side.model.DTO.UserDTO;
import side.side.model.Like;
import side.side.model.UserInfo;
import side.side.repository.LikeRepository;
import side.side.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private UserRepository userRepository;

    // 좋아요 추가
    @Transactional
    public void likeContent(Long userId, String contentId, String contentType) {
        UserInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 이미 좋아요를 눌렀는지 확인
        Optional<Like> existingLike = likeRepository.findByUserAndContentIdAndContentType(user, contentId, contentType);

        if (existingLike.isPresent()) {
            throw new RuntimeException("이미 좋아요를 누른 콘텐츠입니다.");
        }


        Like like = new Like();
        like.setUser(userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다")));
        like.setContentId(contentId);
        like.setContentType(contentType);
        likeRepository.save(like);
    }

    // 좋아요 취소
    @Transactional
    public void unlikeContent(Long userId, String contentId, String contentType) {
        UserInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 좋아요를 취소할 수 있는지 확인
        Like like = likeRepository.findByUserAndContentIdAndContentType(user, contentId, contentType)
                .orElseThrow(() -> new RuntimeException("좋아요 기록이 없습니다."));

        likeRepository.delete(like);
    }

    // 특정 게시물에 대한 좋아요 상태 조회
    public boolean isLiked(Long userId, String contentId) {
        return likeRepository.existsByUserIdAndContentId(userId, contentId);
    }

    // 특정 콘텐츠에 좋아요를 누른 사용자 목록 반환
    public List<UserDTO> getLikedUsers(String contentId) {
        // contentId로 좋아요를 누른 Like 엔티티 목록 조회
        List<Like> likes = likeRepository.findByContentId(contentId);

        // Like 엔티티에서 UserInfo 정보를 가져와 UserDTO로 변환하여 반환
        return likes.stream()
                .map(like -> new UserDTO(
                        like.getUser().getId(),
                        like.getUser().getUserName(),
                        like.getUser().getProfileImage()
                ))
                .collect(Collectors.toList());
    }

    // 특정 콘텐츠에 대한 좋아요 개수 반환
    public int getLikeCount(String contentId, String contentType) {
        return likeRepository.countByContentIdAndContentType(contentId, contentType);
    }

    // 특정 사용자가 좋아요한 콘텐츠 목록 반환
    public List<Like> getLikedContentsByUser(Long userId) {
        UserInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return likeRepository.findByUser(user);
    }

    // 댓글 좋아요 추가
    @Transactional
    public void likeComment(Long userId, Long commentId) {
        UserInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 이미 좋아요를 눌렀는지 확인
        Optional<Like> existingLike = likeRepository.findByUserIdAndContentId(userId, commentId.toString());

        if (existingLike.isPresent()) {
            throw new RuntimeException("이미 좋아요를 누른 댓글입니다.");
        }

        Like like = new Like();
        like.setUser(user);
        like.setContentId(commentId.toString());
        like.setContentType("Comment");
        likeRepository.save(like);
    }

    // 댓글 좋아요 취소
    @Transactional
    public void unlikeComment(Long userId, Long commentId) {
        UserInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Like like = likeRepository.findByUserIdAndContentId(userId, commentId.toString())
                .orElseThrow(() -> new RuntimeException("좋아요 기록이 없습니다."));

        likeRepository.delete(like);
    }

    // 댓글 좋아요 여부 확인
    public boolean isCommentLiked(Long userId, Long commentId) {
        return likeRepository.existsByUserIdAndContentId(userId, commentId.toString());
    }

    // 댓글에 대한 좋아요 개수 반환
    public int getCommentLikeCount(Long commentId) {
        return likeRepository.countByContentIdAndContentType(commentId.toString(), "Comment");
    }
}
