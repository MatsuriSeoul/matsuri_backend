package side.side.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import side.side.service.UserService;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class PasswordRecoveryController {

    @Autowired
    private UserService userService;

    @PostMapping("/recover-password")
    public ResponseEntity<?> recoverPassword(@RequestBody PasswordRecoveryRequest request) {
        Optional<String> password = userService.findPasswordByIdentifier(request.getIdentifier(), request.getOption());
        if (password.isPresent()) {
            return ResponseEntity.ok(new PasswordRecoveryResponse(password.get())); // 200과 비밀번호 반환
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
        }
    }

    @Getter
    @Setter
    public static class PasswordRecoveryRequest {
        private String identifier;
        private String option;
    }

    @Getter
    @AllArgsConstructor
    public static class PasswordRecoveryResponse {
        private String password;
    }
}
