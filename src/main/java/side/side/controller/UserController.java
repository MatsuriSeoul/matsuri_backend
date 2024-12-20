package side.side.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import side.side.config.JwtUtils;
import side.side.model.*;
import side.side.model.DTO.CommentDTO;
import side.side.model.DTO.EventDTO;
import side.side.repository.*;
import side.side.response.LoginResponse;
import side.side.service.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfileImageService profileImageService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private EventService eventService;
    @Autowired
    private CulturalFacilityRepository culturalFacilityRepository;
    @Autowired
    private CulturalFacilityDetailRepository culturalFacilityDetailRepository;
    @Autowired
    private FoodEventRepository foodEventRepository;
    @Autowired
    private FoodEventDetailRepository foodEventDetailRepository;
    @Autowired
    private LeisureSportsEventRepository leisureSportsEventRepository;
    @Autowired
    private LeisureSportsEventDetailRepository leisureSportsEventDetailRepository;
    @Autowired
    private LocalEventRepository localEventRepository;
    @Autowired
    private LocalEventDetailRepository localEventDetailRepository;
    @Autowired
    private ShoppingEventRepository shoppingEventRepository;
    @Autowired
    private ShoppingEventDetailRepository shoppingEventDetailRepository;
    @Autowired
    private TourEventRepository tourEventRepository;
    @Autowired
    private TourEventDetailRepository tourEventDetailRepository;
    @Autowired
    private TouristAttractionRepository touristAttractionRepository;
    @Autowired
    private TouristAttractionDetailRepository touristAttractionDetailRepository;
    @Autowired
    private TravelCourseRepository travelCourseRepository;
    @Autowired
    private TravelCourseDetailRepository travelCourseDetailRepository;

    @Autowired
    private CommentService commentService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private SeoulEventRepository seoulEventRepository;
    @Autowired
    private GyeonggiEventRepository gyeonggiEventRepository;

    @PostMapping("/save")
    public ResponseEntity<String> saveUser(@RequestBody UserInfo userinfo) {
        if (userRepository.existsByUserId(userinfo.getUserId())) {
            // 사용자 ID가 이미 존재하면
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 존재하는 아이디 입니다.");
        }
        if (userService.checkUserEmailExists(userinfo.getUserEmail()) || userService.checkUserPhoneExists(userinfo.getUserPhone())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이메일 또는 휴대폰 번호가 이미 사용 중입니다.");
        }
        // 비밀번호 조건 검증 추가
        try {
            userService.validatePassword(userinfo.getUserPassword());  // 비밀번호 검증 메소드 사용
            userService.saveUser(userinfo);
            log.info("회원가입 성공 userID: {}", userinfo.getUserId());
            return ResponseEntity.ok("환영합니다 ~!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("비밀번호 조건을 만족하지 않습니다: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 저장 오류 : " + e.getMessage());
        }
    }

    // 회원가입시 사용자 role을 'USER'로 설정하기 위한 경로
    @PostMapping("/register")
    public UserInfo registerUser(@RequestBody UserInfo userInfo) {
        return userService.saveUser(userInfo);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserInfo loginRequest) {
        UserInfo user = userService.findByUserId(loginRequest.getUserId());
        if (user != null && loginRequest.getUserPassword().equals(user.getUserPassword())) { // 평문 비교
            String token = jwtUtils.generateToken(user.getUserName(), user.getId(), user.getRole());
            return ResponseEntity.ok(new LoginResponse(token, user.getUserName(), user.getRole()));
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    // 회원가입 and 닉네임 변경 시 중복 검사
    @GetMapping("/check-name/{userName}")
    public  ResponseEntity<?> checkUserName(@PathVariable String userName) {
        boolean exists = userService.checkUserNameExists(userName);
        return ResponseEntity.ok().body(new CheckResponse(exists));
    }

    //  회원가입 시 아이디 중복 검사
    @GetMapping("/check-id/{userId}")
    public ResponseEntity<?> checkUserId(@PathVariable String userId) {
        boolean exists = userService.checkUserIdExists(userId);
        return ResponseEntity.ok().body(new CheckResponse(exists));
    }

    //  회원가입 시 이메일 중복 검사
    @GetMapping("/check-email/{userEmail}")
    public ResponseEntity<?> checkUserEmail(@PathVariable String userEmail) {
        boolean exists = userService.checkUserEmailExists(userEmail);
        return ResponseEntity.ok().body(new CheckResponse(exists));
    }

    //  회원가입 시 전화번호 중복 검사
    @GetMapping("/check-phone/{userPhone}")
    public ResponseEntity<CheckResponse> checkUserPhone(@PathVariable String userPhone) {
        boolean exists = userService.checkUserPhoneExists(userPhone);
        return ResponseEntity.ok(new CheckResponse(exists));
    }

    // 마이페이지 닉네임 변경
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String userName) {
        boolean isDuplicate = userService.isUsernameTaken(userName);
        return ResponseEntity.ok(Collections.singletonMap("isDuplicate", isDuplicate));
    }

    //  인증번호 전송
    @PostMapping("/send-verification-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody VerificationRequest request) {
        boolean isSent = false;
        if ("email".equals(request.getType())) {
            isSent = userService.sendVerificationCodeByEmail(request.getIdentifier());
        } else if ("phone".equals(request.getType())) {
            isSent = userService.sendVerificationCodeByPhone(request.getIdentifier());
        }

        if (isSent) {
            return ResponseEntity.ok("인증번호가 발송되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("인증번호 발송 실패");
        }
    }

    @PostMapping("/verify-code")
    public ResponseEntity<VerifyResponse> verifyCode(@RequestBody VerificationRequest request) {
        boolean verified = userService.verifyCode(request.getIdentifier(), request.getCode());
        return ResponseEntity.ok(new VerifyResponse(verified));
    }

    // 비밀번호 변경 요청 처리
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String token, // JWT 토큰에서 사용자 정보 추출
            @RequestBody PasswordChangeRequest passwordChangeRequest) {

        Long userId = jwtUtils.extractUserId(token);

        try {
            System.out.println("비밀번호 변경 요청을 받았습니다."); // 로그 추가
            boolean isChanged = userService.changePassword(userId, passwordChangeRequest.getCurrentPassword(),
                    passwordChangeRequest.getNewPassword());

            if (!isChanged) {
                System.out.println("비밀번호 변경 실패: 현재 비밀번호 불일치");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("현재 비밀번호가 일치하지 않습니다.");
            }

            // 정상적으로 비밀번호가 변경된 경우 응답
            System.out.println("비밀번호가 성공적으로 변경되었습니다. 응답 코드: 200 OK");
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");

        } catch (IllegalArgumentException e) {
            System.out.println("비밀번호 변경 중 예외 발생: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            System.out.println("서버 오류 발생: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("비밀번호 변경 중 서버 오류가 발생했습니다.");
        }
    }




    //  로그인 한 사용자의 정보를 반환하는 메소드
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            // JWT 토큰에서 사용자 ID 추출
            Long userId = jwtUtils.extractUserId(token);

            // 사용자 정보 조회
            UserInfo user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 정보를 가져오지 못했습니다.");
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserInfo> getUserProfile(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtils.extractUserId(token);
        UserInfo userInfo = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return ResponseEntity.ok(userInfo);
    }

    @PutMapping("/profile")
    public ResponseEntity<String> updateUserProfile(@RequestHeader("Authorization") String token, @RequestBody UserInfo updatedInfo) {
        Long userId = jwtUtils.extractUserId(token);

        try {
            userService.updateUserProfile(userId, updatedInfo);
            return ResponseEntity.ok("프로필이 업데이트되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("프로필 업데이트에 실패했습니다: " + e.getMessage());
        }
    }

    //  프로필 이미지 저장 (마이페이지 내에서)
    @PostMapping("/profile-image")
    public ResponseEntity<String> updateProfileImage(
            @RequestHeader("Authorization") String token,
            @RequestParam("image") MultipartFile imageFile) {

        Long userId = jwtUtils.extractUserId(token);

        try {
            // 파일 저장 로직
            String fileName = profileImageService.uploadProfileImage(imageFile);

            // 사용자 프로필 이미지 업데이트
            userService.updateUserProfileImage(userId, fileName);

            return ResponseEntity.ok("프로필 이미지가 업데이트되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("프로필 이미지 업데이트 실패");
        }
    }

    //  프로필 이미지 삭제
    @DeleteMapping("/profile-image")
    public ResponseEntity<String> deleteProfileImage(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtils.extractUserId(token);

        try {
            // 사용자 프로필 이미지 삭제
            userService.deleteUserProfileImage(userId);

            return ResponseEntity.ok("프로필 이미지가 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("프로필 이미지 삭제 실패");
        }
    }

    // 사용자가 좋아요를 누른 게시글 목록 조회
    @GetMapping("/liked-events")
    public ResponseEntity<List<EventDTO>> getLikedEvents(@RequestHeader("Authorization") String token) {
        // JWT에서 사용자 ID 추출
        Long userId = jwtUtils.extractUserId(token);
        List<Like> likedContents = likeService.getLikedContentsByUser(userId);

        // 각 좋아요 항목을 EventDTO로 매핑하여 반환
        List<EventDTO> likedEvents = likedContents.stream()
                .map(like -> {
                    String contentId = like.getContentId();
                    String contentType = like.getContentType();

                    // contentType이 "EventDetail"인 경우
                    if ("EventDetail".equals(contentType)) {
                        return eventService.findEventDetailFromAllSources(contentId); // 모든 테이블에서 검색
                    }

                    // 각 contentType에 따른 검색
                    switch (contentType) {
                        case "CulturalFacilityDetail":
                        case "FoodEventDetail":
                        case "LeisureSportsEventDetail":
                        case "LocalEventDetail":
                        case "ShoppingEventDetail":
                        case "TourEventDetail":
                        case "TouristAttractionDetail":
                        case "TravelCourseDetail":
                            return eventService.findEventDetailFromAllSources(contentId);  // 통합 조회
                        case "SeoulEventDetail":  // 서울 이벤트 조회
                            return eventService.findEventDetailFromAllSources(contentId);
                        case "GyeonggiEventDetail":  // 경기 이벤트 조회
                            return eventService.findEventDetailFromAllSources(contentId);
                        default:
                            return null;
                    }
                })
                .filter(eventDTO -> eventDTO != null)  // null 값을 필터링
                .collect(Collectors.toList());

        // 디버그용: contenttypeid를 포함해 반환되는 데이터를 확인합니다.
        likedEvents.forEach(event -> System.out.println(event.getContenttypeid()));


        return ResponseEntity.ok(likedEvents);
    }

    //사용자가 좋아요 누른 댓글목록 조회
    @GetMapping("/liked-comments")
    public ResponseEntity<List<CommentDTO>> getLikedComments(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtils.extractUserId(token);
        List<Like> likedComments = likeService.getLikedContentsByUser(userId)
                .stream()
                .filter(like -> "Comment".equals(like.getContentType()))
                .collect(Collectors.toUnmodifiableList());

        List<CommentDTO> comments = likedComments.stream()
                .map(like -> commentService.findCommentById(Long.parseLong(like.getContentId())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(comment -> {
                    String contenttypeid = null;
                    String contentid = null;
                    String svcid = null;
                    Long id = null;

                    // 각 댓글에 달린 콘텐츠나 이벤트에서 contenttypeid 가져오기
                    if (comment.getNotice() != null) {
                        contenttypeid = "Notice";  // 공지사항에 대한 댓글일 경우
                    } else if (comment.getCategory() != null) {
                        // 카테고리에 따른 contenttypeid 설정
                        switch (comment.getCategory()) {
                            case "seoul-events":
                                svcid = comment.getSvcid();
                                contenttypeid = eventService.findEventDetailFromAllSources(comment.getSvcid()).getContenttypeid();
                                break;
                            case "gyeonggi-events":
                                id = comment.getGyeonggiEvent().getId();
                                contentid = comment.getGyeonggiEvent().getId().toString();
                                contenttypeid = eventService.findEventDetailFromAllSources(id.toString()).getContenttypeid();
                                break;
                            default:
                                contentid = comment.getContentid();
                                contenttypeid = eventService.findEventDetailFromAllSources(contentid).getContenttypeid();
                        }
                    }

                    // CommentDTO로 변환하면서 contenttypeid, contentid, svcid, id 추가
                    return new CommentDTO(comment, contenttypeid, contentid, svcid, id);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(comments);
    }

    // 내가 작성한 댓글 조회
    @GetMapping("/authored-comments")
    public ResponseEntity<List<CommentDTO>> getAuthoredComments(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtils.extractUserId(token);
        List<Comment> authoredComments = commentService.getCommentsByUserId(userId);

        // 댓글을 CommentDTO로 매핑하면서 contenttypeid를 설정
        List<CommentDTO> authoredCommentsWithContentTypeId = authoredComments.stream()
                .filter(comment -> comment.getNotice() == null) // 공지사항 댓글 제외
                .map(comment -> {
                    String contenttypeid = null;
                    String contentid = null;
                    String svcid = null;
                    Long id = null;

                    // 각 댓글에 달린 콘텐츠나 이벤트에서 contenttypeid 가져오기
                    if (comment.getCategory() != null) {
                        // 카테고리에 따른 contenttypeid 설정
                        switch (comment.getCategory()) {
                            case "seoul-events":
                                svcid = comment.getSvcid();
                                contenttypeid = eventService.findEventDetailFromAllSources(comment.getSvcid()).getContenttypeid();
                                break;
                            case "gyeonggi-events":
                                id = comment.getGyeonggiEvent().getId();
                                contentid = comment.getGyeonggiEvent().getId().toString();
                                contenttypeid = eventService.findEventDetailFromAllSources(id.toString()).getContenttypeid();
                                break;
                            default:
                                contentid = comment.getContentid();
                                contenttypeid = eventService.findEventDetailFromAllSources(contentid).getContenttypeid();
                        }
                    }

                    // CommentDTO로 변환하면서 contenttypeid 추가
                    return new CommentDTO(comment, contenttypeid, contentid, svcid, id);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(authoredCommentsWithContentTypeId);
    }




    @Getter
    @Setter
    public static class PasswordChangeRequest {
        private String currentPassword;
        private String newPassword;
    }


        @Getter
    @Setter
    public static class VerificationRequest {
        private String identifier;  // 이메일 또는 전화번호
        private String type;         // "email" 또는 "phone"
        private String code;         // 인증번호 (검증할 때 사용)
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class VerifyResponse {
        private boolean verified;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class CheckResponse {
        private boolean exists;
    }


}

