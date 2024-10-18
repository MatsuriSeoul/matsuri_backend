package side.side.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import side.side.model.Comment;
import side.side.model.CommentImage;
import side.side.model.GyeonggiEvent;
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

    // 이미지가 저장되는 디렉토리 경로
    private final String uploadDir = System.getProperty("user.home") + "/Desktop/uploads/commentImage/";

    // 이미지 파일 업로드
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

    // 이미지 삭제
    public void removeCommentImages(List<Long> imageIds) {
        for (Long imageId : imageIds) {
            commentImageRepository.deleteById(imageId);
        }
    }

    public void deleteCommentImage(Long imageId, Long userId) {
        CommentImage commentImage = commentImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("이미지를 찾을 수 없습니다."));

        // 이미지가 속한 댓글의 작성자와 현재 사용자 비교 (권한 확인)
        if (!commentImage.getComment().getAuthor().getId().equals(userId)) {
            throw new RuntimeException("이미지 삭제 권한이 없습니다.");
        }

        // 로컬 파일 시스템에서 이미지 삭제
        String imagePath = uploadDir + commentImage.getImgName();
        try {
            Files.deleteIfExists(Paths.get(imagePath));
        } catch (IOException e) {
            throw new RuntimeException("이미지 파일 삭제 실패: " + e.getMessage());
        }

        // 데이터베이스에서 이미지 삭제
        commentImageRepository.delete(commentImage);
    }

    // 댓글 작성 시 이미지 저장
    public void saveCommentImages(Comment comment, List<MultipartFile> images, String category, String idForImage) throws IOException {
        if (images == null || images.isEmpty()) return;

        for (MultipartFile image : images) {
            String imagePath = uploadCommentImage(image);  // 이미지 업로드 후 경로 가져오기
            CommentImage commentImage = new CommentImage();
            commentImage.setImgName(image.getOriginalFilename());
            commentImage.setImagePath(imagePath);
            commentImage.setComment(comment);
            commentImage.setCategory(category);

            // 카테고리별 이미지 설정
            if ("seoul-events".equals(category)) {
                commentImage.setSvcid(idForImage);
            } else if ("gyeonggi-events".equals(category)) {
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

    // 댓글 생성
    public Comment createComment(Comment comment) {
        // 경기 이벤트 댓글일 경우 contentid를 설정하지 않음
        if (comment.getGyeonggiEvent() != null) {
            comment.setContentid(null);
        }

        return commentRepository.save(comment);
    }

    // 특정 공지사항의 댓글 조회
    public List<Comment> getCommentByNoticeId(Long noticeId) {
        return commentRepository.findByNoticeId(noticeId);
    }

    // 댓글 수정
    public Comment updateComment(Long commentId, String content, Long userId, List<MultipartFile> newImages) throws IOException {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 작성자 확인
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        comment.setContent(content);

        // 기존 댓글의 category 및 관련 정보를 가져와 이미지 추가 처리
        if (newImages != null && !newImages.isEmpty()) {
            String idForImage = comment.getSvcid() != null ? comment.getSvcid() : (comment.getContentid() != null ? comment.getContentid() : String.valueOf(comment.getGyeonggiEvent().getId()));
            saveCommentImages(comment, newImages, comment.getCategory(), idForImage);
        }

        return commentRepository.save(comment);
    }

    // 댓글 삭제
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 댓글 작성자 확인
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }

    public List<Comment> getCommentsByUserId(Long userId) {
        return commentRepository.findByAuthorId(userId);
    }

    // 특정 이벤트에 대한 댓글 조회
    public List<Comment> getCommentByEventId(String contentid) {
        return commentRepository.findBycontentid(contentid);
    }

    // 관광지에 대한 댓글 조회
    public List<Comment> getCommentByTouristAttraction(String contentid) {
        return commentRepository.findBycontentid(contentid);
    }

    // 문화시설에 대한 댓글 조회
    public List<Comment> getCommentByCulturalFacility(String contentid) {
        return commentRepository.findBycontentid(contentid);
    }

    // 여행 코스에 대한 댓글 조회
    public List<Comment> getCommentByTravelCourse(String contentid) {
        return commentRepository.findBycontentid(contentid);
    }

    // 레저 스포츠에 대한 댓글 조회
    public List<Comment> getCommentByLeisureSports(String contentid) {
        return commentRepository.findBycontentid(contentid);
    }

    // 숙박시설에 대한 댓글 조회
    public List<Comment> getCommentByAccommodation(String contentid) {
        return commentRepository.findBycontentid(contentid);
    }

    // 쇼핑에 대한 댓글 조회
    public List<Comment> getCommentByShopping(String contentid) {
        return commentRepository.findBycontentid(contentid);
    }

    // 음식점에 대한 댓글 조회
    public List<Comment> getCommentByFood(String contentid) {
        return commentRepository.findBycontentid(contentid);
    }

    // 구역에 대한 댓글 조회
    public List<Comment> getCommentByDistrict(String contentid) {
        return commentRepository.findBycontentid(contentid);
    }

    // 댓글 ID로 댓글 조회
    public Optional<Comment> findCommentById(long id) {
        return commentRepository.findById(id);
    }

    // 서울 이벤트에 대한 댓글 조회
    public List<Comment> getCommentBySeoulEvent(String svcid) {
        return commentRepository.findBySvcid(svcid);
    }

    // 경기도 이벤트에 대한 댓글 조회
    public List<Comment> getCommentByGyeonggiEventId(Long gyeonggiEventId) {
        return commentRepository.findByGyeonggiEventId(gyeonggiEventId);
    }

}
