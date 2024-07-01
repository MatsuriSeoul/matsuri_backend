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
public class UserRecoveryController {

    @Autowired
    private UserService userService;

    @PostMapping("/recovery-userid")
    public ResponseEntity<?> recoverUserId(@RequestBody UserIdRecoveryRequest request) {
        Optional<String> userId;
        if ("email".equals(request.getOption())) {
            userId = userService.findUserIdByEmail(request.getIdentifier());
        } else {
            userId = userService.findUserIdByPhone(request.getIdentifier());
        }
        if (userId.isPresent()) {
            return ResponseEntity.ok(new UserIdRecoveryResponse(userId.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당하는 사용자 정보가 없습니다");
        }
    }

    @Getter
    @Setter
    public static class UserIdRecoveryRequest {
        private String identifier;
        private String option;
    }

    @Getter
    @AllArgsConstructor
    public static class UserIdRecoveryResponse {
        private String userId;
    }
}
