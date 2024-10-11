package side.side.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import side.side.config.JwtUtils;
import side.side.model.*;
import side.side.service.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    // 댓글 작성
    @PostMapping
    public ResponseEntity<?> createComment(
            @RequestParam("content") String content,
            @RequestParam(value = "noticeId", required = false) Long noticeId,
            @RequestParam(value = "contentid", required = false) String contentid,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestHeader("Authorization") String token) {
        try {
            // JWT 토큰에서 사용자 정보 추출
            Long userId = jwtUtils.extractUserId(token);

            // 사용자 정보 조회
            UserInfo user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 이름 마스킹 처리 (예: 홍길동 -> 홍OO)
            String maskedName = maskName(user.getUserName());

            // 댓글 생성 및 저장
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setAuthor(user);  // 댓글 작성자 정보 저장
            comment.setMaskedAuthor(maskedName);

            // 공지사항 댓글 작성
            if (noticeId != null) {
                Notice notice = noticeService.getNoticeById(noticeId)
                        .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
                comment.setNotice(notice);
            }

            // 행사, 문화시설 등 다른 콘텐츠에 대한 댓글 작성
            switch (category) {
                case "events":
                    List<TourEvent> events = tourEventService.findBycontentid(contentid);
                    if (events.isEmpty()) {
                        throw new RuntimeException("해당 콘텐츠 ID에 대한 이벤트를 찾을 수 없습니다.");
                    }
                    break;

                case "cultural-facilities":
                    List<CulturalFacility> culturalFacilities = culturalFacilityService.findBycontentid(contentid);
                    if (culturalFacilities.isEmpty()) {
                        throw new RuntimeException("해당 콘텐츠 ID에 대한 문화시설을 찾을 수 없습니다.");
                    }
                    break;

                case "tourist-attraction":
                    List<TouristAttraction> touristAttractions = touristAttractionsService.findBycontentid(contentid);
                    if (touristAttractions.isEmpty()) {
                        throw new RuntimeException("해당 콘텐츠 ID에 대한 관광지를 찾을 수 없습니다.");
                    }
                    break;

                case "travel-courses":
                    List<TravelCourse> travelCourses = travelCourseService.findBycontentid(contentid);
                    if (travelCourses.isEmpty()) {
                        throw new RuntimeException("해당 콘텐츠 ID에 대한 여행 코스를 찾을 수 없습니다.");
                    }
                    break;

                case "leisure-sports":
                    List<LeisureSportsEvent> leisureSports = leisureSportsEventService.findBycontentid(contentid);
                    if (leisureSports.isEmpty()) {
                        throw new RuntimeException("해당 콘텐츠 ID에 대한 레저 스포츠를 찾을 수 없습니다.");
                    }
                    break;

                case "local-events":
                    List<LocalEvent> localEvents = localEventService.findBycontentid(contentid);
                    if (localEvents.isEmpty()) {
                        throw new RuntimeException("해당 콘텐츠 ID에 대한 지역 행사를 찾을 수 없습니다.");
                    }
                    break;

                case "shopping-events":
                    List<ShoppingEvent> shoppingEvents = shoppingEventService.findBycontentid(contentid);
                    if (shoppingEvents.isEmpty()) {
                        throw new RuntimeException("해당 콘텐츠 ID에 대한 쇼핑 이벤트를 찾을 수 없습니다.");
                    }
                    break;

                case "food-events":
                    List<FoodEvent> foodEvents = foodEventService.findBycontentid(contentid);
                    if (foodEvents.isEmpty()) {
                        throw new RuntimeException("해당 콘텐츠 ID에 대한 음식 이벤트를 찾을 수 없습니다.");
                    }
                    break;

                default:
                    throw new RuntimeException("알 수 없는 카테고리입니다.");
            }


            // 댓글 저장
            Comment savedComment = commentService.createComment(comment);

            // 이미지 저장
            if (images != null && !images.isEmpty()) {
                commentService.saveCommentImages(savedComment, images, category, contentid);
            }

            return ResponseEntity.ok("댓글이 작성되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 작성 실패: " + e.getMessage());
        }
    }

    // 이름 마스킹
    private String maskName(String name) {
        if (name == null || name.length() < 2) {
            return name;  // 이름이 너무 짧거나 없는 경우 그대로 반환
        }

        // 성 뒤에 모든 글자를 O로 마스킹 처리 (홍길동 -> 홍OO)
        return name.charAt(0) + "O".repeat(name.length() - 1);
    }

    // 댓글 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updateComment(@PathVariable Long id, @RequestBody Map<String, String> payload, @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtils.extractUserId(token);  // JWT에서 사용자 ID 추출
            String newContent = payload.get("content");

            // 작성자 확인 및 댓글 수정
            Comment updatedComment = commentService.updateComment(id, newContent, userId);
            return ResponseEntity.ok(updatedComment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 수정 실패");
        }
    }

    // 댓글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtils.extractUserId(token);  // JWT에서 사용자 ID 추출
            commentService.deleteComment(id, userId);  // 작성자 확인 후 삭제
            return ResponseEntity.ok("댓글이 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 삭제 실패");
        }
    }

    // 특정 공지사항의 댓글 가져오기
    @GetMapping("/notice/{noticeId}")
    public List<Comment> getCommentByNoticeId(@PathVariable Long noticeId) {
        return commentService.getCommentByNoticeId(noticeId);
    }

    @GetMapping("/{category}/{contentid}/{contenttypeid}/detail")
    public ResponseEntity<List<Comment>> getCommentsByCategory(
            @PathVariable String category,
            @PathVariable String contentid
    ) {
        try {
            List<Comment> comments;
            switch (category) {
                case "tourist-attraction":
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
                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/upload-comment-image")
    public ResponseEntity<?> uploadCommentImage(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = commentService.uploadCommentImage(file);  // 파일 경로 설정
            return ResponseEntity.ok().body(filePath);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 업로드 실패");
        }
    }
}
