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
@RequestMapping("/api/password-recovery")
@AllArgsConstructor
@Slf4j
public class PasswordRecoveryController {

    @Autowired
    private UserService userService;

    /**
     * 인증번호 발송 요청 (아이디 + 이메일 또는 전화번호)
     */
    @PostMapping("/send-verification-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody VerificationRequest request) {
        try {
            boolean isSent;

            log.debug("Received request for verification code. UserId: {}, Identifier: {}, Option: {}",
                    request.getUserId(), request.getIdentifier(), request.getOption());

            // 인증 방식에 따라 처리
            if ("phone".equals(request.getOption())) {
                isSent = userService.sendVerificationCodeByPhoneForPassword(request.getUserId(), request.getIdentifier());
            } else if ("email".equals(request.getOption())) {
                isSent = userService.sendVerificationCodeByEmailForPassword(request.getUserId(), request.getIdentifier());
            } else {
                return ResponseEntity.badRequest().body("유효하지 않은 인증 방식입니다.");
            }

            return isSent ? ResponseEntity.ok("인증번호가 발송되었습니다.") : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("인증번호 발송 실패");

        } catch (Exception e) {
            log.error("인증번호 전송 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
        }
    }

    /**
     * 비밀번호 재설정 요청 (아이디 + 이메일 또는 전화번호 + 인증번호)
     */
    @PostMapping("/recover-password")
    public ResponseEntity<PasswordRecoveryResponse> recoverPassword(@RequestBody PasswordRecoveryRequest request) {
        log.info("Received request to recover password. UserId: {}, Identifier: {}, Option: {}, Code: {}",
                request.getUserId(), request.getIdentifier(), request.getOption(), request.getCode());

        try {
            Optional<String> tempPassword = userService.resetPassword(request.getUserId(), request.getIdentifier(), request.getOption());

            if (tempPassword.isPresent()) {
                return ResponseEntity.ok(new PasswordRecoveryResponse(tempPassword.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new PasswordRecoveryResponse("인증에 실패했거나 사용자를 찾을 수 없습니다."));
            }
        } catch (Exception e) {
            log.error("비밀번호 재설정 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PasswordRecoveryResponse("비밀번호 재설정에 실패했습니다. 다시 시도해 주세요."));
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



    @Getter
    @Setter
    public static class VerificationRequest {
        private String userId;      // 사용자 아이디
        private String identifier;  // 이메일 또는 전화번호
        private String option;      // 인증 방식 ("email" 또는 "phone")
        private String code;        // 인증번호
    }

    @Getter
    @Setter
    public static class PasswordRecoveryRequest {
        private String userId;      // 사용자 아이디
        private String identifier;  // 이메일 또는 전화번호
        private String option;      // 인증 방식 ("email" 또는 "phone")
        private String code;        // 인증번호
    }

    @Getter
    @AllArgsConstructor
    public static class PasswordRecoveryResponse {
        private String password;
    }
}
