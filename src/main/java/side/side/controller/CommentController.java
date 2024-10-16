package side.side.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import side.side.config.JwtUtils;
import side.side.model.*;
import side.side.service.*;

import java.util.List;

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

    @Autowired
    private CulturalFacilityService culturalFacilityService;

    @Autowired
    private TouristAttractionsService touristAttractionsService;

    @Autowired
    private ShoppingEventService shoppingEventService;

    @Autowired
    private TravelCourseService travelCourseService;

    @Autowired
    private LeisureSportsEventService leisureSportsEventService;

    @Autowired
    private LocalEventService localEventService;

    @Autowired
    private FoodEventService foodEventService;

    @Autowired
    private LocalBasedService localBasedService;

    @Autowired
    private SeoulEventService seoulEventService;

    @Autowired
    private GyeonggiEventService gyeonggiEventService;

    // 댓글 작성
    @PostMapping
    public ResponseEntity<?> createComment(
            @RequestParam("content") String content,
            @RequestParam(value = "noticeId", required = false) Long noticeId,
            @RequestParam(value = "contentid", required = false) String contentid,
            @RequestParam(value = "svcid", required = false) String svcid,
            @RequestParam(value = "gyeonggiEventId", required = false) Long gyeonggiEventId,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtils.extractUserId(token);
            UserInfo user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            String maskedName = maskName(user.getUserName());

            // 댓글 생성 및 저장
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setAuthor(user);
            comment.setMaskedAuthor(maskedName);

            // 공지사항 댓글
            if (noticeId != null) {
                Notice notice = noticeService.getNoticeById(noticeId)
                        .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
                comment.setNotice(notice);
            }

            // 경기도 이벤트 댓글
            if (gyeonggiEventId != null) {
                GyeonggiEvent gyeonggiEvent = gyeonggiEventService.findById(gyeonggiEventId)
                        .orElseThrow(() -> new RuntimeException("경기도 이벤트를 찾을 수 없습니다."));
                comment.setGyeonggiEvent(gyeonggiEvent);
            } else if ("gyeonggi-events".equals(category) && contentid != null) {
                GyeonggiEvent gyeonggiEvent = gyeonggiEventService.findById(Long.parseLong(contentid))
                        .orElseThrow(() -> new RuntimeException("경기도 이벤트를 찾을 수 없습니다."));
                comment.setGyeonggiEvent(gyeonggiEvent);
            } else if (category != null) {
                boolean isValidCategory = validateCategory(category, contentid, svcid, gyeonggiEventId);
                if (!isValidCategory) {
                    throw new RuntimeException("해당 콘텐츠에 대한 데이터를 찾을 수 없습니다.");
                }
                comment.setContentid(contentid);
                comment.setSvcid(svcid);
                comment.setCategory(category);
            }

            Comment savedComment = commentService.createComment(comment);

            String idForImage = svcid != null ? svcid : (contentid != null ? contentid : String.valueOf(gyeonggiEventId));
            if (images != null && !images.isEmpty()) {
                commentService.saveCommentImages(savedComment, images, category, idForImage);
            }

            return ResponseEntity.ok("댓글이 작성되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 작성 실패: " + e.getMessage());
        }
    }

    // 카테고리 검증
    private boolean validateCategory(String category, String contentid, String svcid, Long gyeonggiEventId) {
        switch (category) {
            case "events":
                return !tourEventService.findBycontentid(contentid).isEmpty();
            case "cultural-facilities":
                return !culturalFacilityService.findBycontentid(contentid).isEmpty();
            case "tourist-attractions":
                return !touristAttractionsService.findBycontentid(contentid).isEmpty();
            case "travel-courses":
                return !travelCourseService.findBycontentid(contentid).isEmpty();
            case "leisure-sports":
                return !leisureSportsEventService.findBycontentid(contentid).isEmpty();
            case "local-events":
                return !localEventService.findBycontentid(contentid).isEmpty();
            case "shopping-events":
                return !shoppingEventService.findBycontentid(contentid).isEmpty();
            case "food-events":
                return !foodEventService.findBycontentid(contentid).isEmpty();
            case "district":
                return !localBasedService.findBycontentid(contentid).isEmpty();
            case "seoul-events":
                return seoulEventService.findBySvcid(svcid) != null;
            case "gyeonggi-events":
                return gyeonggiEventService.findById(gyeonggiEventId).isPresent();
            default:
                return false;
        }
    }

    // 이름 마스킹
    private String maskName(String name) {
        if (name == null || name.length() < 2) {
            return name;
        }
        return name.charAt(0) + "O".repeat(name.length() - 1);
    }

    // 댓글 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long id,
            @RequestParam("content") String newContent,
            @RequestParam(value = "newImages", required = false) List<MultipartFile> newImages,
            @RequestParam(value = "removeImageIds", required = false) List<Long> removeImageIds,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtils.extractUserId(token);

            // 댓글 수정
            Comment updatedComment = commentService.updateComment(id, newContent, userId, newImages);

            // 이미지 삭제
            if (removeImageIds != null && !removeImageIds.isEmpty()) {
                commentService.removeCommentImages(removeImageIds);
            }

            return ResponseEntity.ok(updatedComment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 수정 실패: " + e.getMessage());
        }
    }

    // 댓글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtils.extractUserId(token);
            commentService.deleteComment(id, userId);
            return ResponseEntity.ok("댓글이 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 삭제 실패");
        }
    }

    @DeleteMapping("/image/{imageId}")
    public ResponseEntity<?> deleteCommentImage(@PathVariable Long imageId, @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtils.extractUserId(token);  // JWT에서 사용자 정보 추출
            commentService.deleteCommentImage(imageId, userId);  // 이미지 삭제 서비스 호출
            return ResponseEntity.ok("이미지가 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 삭제 실패: " + e.getMessage());
        }
    }

    // 특정 공지사항의 댓글 가져오기
    @GetMapping("/notice/{noticeId}")
    public List<Comment> getCommentByNoticeId(@PathVariable Long noticeId) {
        return commentService.getCommentByNoticeId(noticeId);
    }

    @GetMapping("/gyeonggi-events/{gyeonggiEventId}/detail")
    public ResponseEntity<List<Comment>> getCommentsByGyeonggiEvent(@PathVariable Long gyeonggiEventId) {
        try {
            List<Comment> comments = commentService.getCommentByGyeonggiEventId(gyeonggiEventId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/seoul-events/{svcid}/detail")
    public ResponseEntity<List<Comment>> getCommentsBySeoulEvent(@PathVariable String svcid) {
        try {
            List<Comment> comments = commentService.getCommentBySeoulEvent(svcid);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/{category}/{contentid}/{contenttypeid}/detail")
    public ResponseEntity<List<Comment>> getCommentsByCategory(@PathVariable String category, @PathVariable String contentid) {
        try {
            List<Comment> comments;
            switch (category) {
                case "tourist-attractions":
                    comments = commentService.getCommentByTouristAttraction(contentid);
                    break;
                case "cultural-facilities":
                    comments = commentService.getCommentByCulturalFacility(contentid);
                    break;
                case "events":
                    comments = commentService.getCommentByEventId(contentid);
                    break;
                case "travel-courses":
                    comments = commentService.getCommentByTravelCourse(contentid);
                    break;
                case "leisure-sports":
                    comments = commentService.getCommentByLeisureSports(contentid);
                    break;
                case "local-events":
                    comments = commentService.getCommentByAccommodation(contentid);
                    break;
                case "shopping-events":
                    comments = commentService.getCommentByShopping(contentid);
                    break;
                case "food-events":
                    comments = commentService.getCommentByFood(contentid);
                    break;
                case "district":
                    comments = commentService.getCommentByDistrict(contentid);
                    break;
                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // 이미지 업로드
    @PostMapping("/upload-comment-image")
    public ResponseEntity<?> uploadCommentImage(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = commentService.uploadCommentImage(file);
            return ResponseEntity.ok().body(filePath);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 업로드 실패");
        }
    }
}
