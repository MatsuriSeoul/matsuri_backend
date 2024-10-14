package side.side.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import side.side.model.Comment;
import side.side.model.CommentImage;
import side.side.model.GyeonggiEvent;
import side.side.model.TourEvent;
import side.side.repository.CommentImageRepository;
import side.side.repository.CommentRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentImageRepository commentImageRepository;

    @Autowired
    private GyeonggiEventService gyeonggiEventService;

    private final String uploadDir = System.getProperty("user.home") + "/Desktop/uploads/commentImage/";

    public String uploadCommentImage(MultipartFile image) throws IOException {
        // 파일 이름 생성
        String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
        // 파일 경로 생성
        Path filePath = Paths.get(uploadDir + fileName);

        // 디렉토리 생성
        Files.createDirectories(filePath.getParent());
        // 파일 저장
        Files.write(filePath, image.getBytes());

        return "/uploads/commentImage/" + fileName;
    }

    // 댓글 작성 시 이미지 저장
    public void saveCommentImages(Comment comment, List<MultipartFile> images, String category, String idForImage) throws IOException {
        if (images == null || images.isEmpty()) return;

        for (MultipartFile image : images) {
            String imagePath = uploadCommentImage(image);
            CommentImage commentImage = new CommentImage();
            commentImage.setImgName(image.getOriginalFilename());
            commentImage.setImagePath(imagePath);
            commentImage.setComment(comment);
            commentImage.setCategory(category);

            if (category.equals("seoul-events")) {
                commentImage.setSvcid(idForImage);
            } else if (category.equals("gyeonggi-events")) {
                Long gyeonggiEventId = Long.parseLong(idForImage);
                GyeonggiEvent gyeonggiEvent = gyeonggiEventService.findById(gyeonggiEventId)
                        .orElseThrow(() -> new RuntimeException("경기도 이벤트를 찾을 수 없습니다."));
                commentImage.setGyeonggiEvent(gyeonggiEvent);
            } else {
                commentImage.setContentid(idForImage);
            }

            commentImageRepository.save(commentImage);
        }
    }


    public Comment createComment(Comment comment) {

        // 경기 이벤트 댓글일 경우 contentid를 설정하지 않음
        if (comment.getGyeonggiEvent() != null) {
            comment.setContentid(null);
        }

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

    public List<Comment> getCommentByEventId(String contentid) {
        return commentRepository.findBycontentid(contentid);
    }

    public List<Comment> getCommentByTouristAttraction(String contentid) {
        return commentRepository.findBycontentid(contentid);
    }

    public List<Comment> getCommentByCulturalFacility(String contentid) {
        return commentRepository.findBycontentid(contentid);
    }

    public List<Comment> getCommentByTravelCourse(String contentid) {
        return commentRepository.findBycontentid(contentid);
    }

    public List<Comment> getCommentByLeisureSports(String contentid) {
        return commentRepository.findBycontentid(contentid);
    }

    public List<Comment> getCommentByAccommodation(String contentid) {
        return commentRepository.findBycontentid(contentid);
    }

    public List<Comment> getCommentByShopping(String contentid) {
        return commentRepository.findBycontentid(contentid);
    }

    public List<Comment> getCommentByFood(String contentid) {
        return commentRepository.findBycontentid(contentid);
    }

    public List<Comment> getCommentByDistrict(String contentid) {
        return commentRepository.findBycontentid(contentid);
    }

    public Optional<Comment> findCommentById(long id){
        return commentRepository.findById(id);
    }

    public List<Comment> getCommentBySeoulEvent(String svcid) {
        return commentRepository.findBySvcid(svcid);
    }

    public List<Comment> getCommentByGyeonggiEventId(Long gyeonggiEventId) {
        return commentRepository.findByGyeonggiEventId(gyeonggiEventId);
    }

}
