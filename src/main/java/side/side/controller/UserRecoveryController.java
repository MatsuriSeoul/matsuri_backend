package side.side.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.service.UserService;

import java.util.Optional;

@RestController
@RequestMapping("/api/user-recovery")
@AllArgsConstructor
@Slf4j
public class UserRecoveryController {

    @Autowired
    private UserService userService;

    /**
     * 인증번호 발송
     * @param request - 인증요청 데이터 (이름 + 전화번호 또는 이메일)
     * @return 응답 메시지
     */
    @PostMapping("/send-verification-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody VerificationRequest request) {
        try {
            boolean isSent;

            log.debug("Received request for verification code. name: {}, identifier: {}, option: {}",
                    request.getName(), request.getIdentifier(), request.getOption());

            // 인증 방식에 따라 처리
            if ("phone".equals(request.getOption())) {
                isSent = userService.sendVerificationCodeByPhoneForUserName(request.getName(), request.getIdentifier());
            } else if ("email".equals(request.getOption())) {
                isSent = userService.sendVerificationCodeByEmailForUserName(request.getName(), request.getIdentifier());
            } else {
                return ResponseEntity.badRequest().body("유효하지 않은 인증 방식입니다.");
            }

            return isSent ? ResponseEntity.ok("인증번호가 발송되었습니다.") : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("인증번호 발송 실패");

        } catch (Exception e) {
            // 예외가 발생했을 때, 로그를 남기고 500 에러를 반환
            log.error("인증번호 전송 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
        }
    }

    /**
     * 인증번호 검증
     */
    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody VerificationRequest request) {
        log.info("Verifying code for identifier: {}", request.getIdentifier());
        boolean isVerified = userService.verifyCode(request.getIdentifier(), request.getCode());
        if (isVerified) {
            return ResponseEntity.ok("인증 성공");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증 실패");
        }
    }



    /**
     * 사용자 아이디 복구
     * @param request - 복구 요청 데이터 (이름 + 이메일/전화번호 + 인증번호)
     * @return 복구된 사용자 아이디
     */
    @PostMapping("/recover-userid")
    public ResponseEntity<?> recoverUserId(@RequestBody UserIdRecoveryRequest request) {
        log.info("Starting userId recovery with name: {}, identifier: {}, option: {}, code: {}",
                request.getName(), request.getIdentifier(), request.getOption(), request.getCode());

        Optional<String> userId;

        if ("phone".equals(request.getOption())) {
            userId = userService.findUserIdByPhone(request.getName(), request.getIdentifier());
        } else if ("email".equals(request.getOption())) {
            userId = userService.findUserIdByEmail(request.getName(), request.getIdentifier());
        } else {
            return ResponseEntity.badRequest().body("유효하지 않은 인증 방식입니다.");
        }

        if (userId.isPresent()) {
            log.info("UserId found for name: {}, identifier: {}", request.getName(), request.getIdentifier());
            return ResponseEntity.ok(new UserIdRecoveryResponse(userId.get()));
        } else {
            log.warn("UserId not found or verification failed for name: {}, identifier: {}", request.getName(), request.getIdentifier());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("인증에 실패했거나 사용자를 찾을 수 없습니다.");
        }
    }


    @Getter
    @Setter
    public static class VerificationRequest {
        private String name;        // 사용자 이름
        private String identifier;  // 이메일 또는 전화번호
        private String option;      // 인증 방식 ("email" 또는 "phone")
        private String code;        // 인증번호
    }

    @Getter
    @Setter
    public static class UserIdRecoveryRequest {
        private String name;        // 사용자 이름
        private String identifier;  // 이메일 또는 전화번호
        private String option;      // 인증 방식 ("phone" 또는 "email")
        private String code;        // 인증번호
    }

    @Getter
    @AllArgsConstructor
    public static class UserIdRecoveryResponse {
        private String userId;
    }
}
