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
import side.side.model.UserInfo;
import side.side.repository.UserRepository;
import side.side.response.LoginResponse;
import side.side.service.ProfileImageService;
import side.side.service.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

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

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

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

