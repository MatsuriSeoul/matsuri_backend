package side.side.controller;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogoutController {
    private static final Logger logger = LoggerFactory.getLogger(LogoutController.class);

    @PostMapping("/api/logout")
    public void logUserLogout(@RequestBody LogoutRequest logoutRequest) {
        logger.info("닉네임 : {} 님이 로그아웃 하였습니다.", logoutRequest.getUserNick());
    }

    @Getter
    @Setter
    public static class LogoutRequest {
        private String userNick;
        private Long userId;
    }
}
