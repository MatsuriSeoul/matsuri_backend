package side.side.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import side.side.service.SocialLoginService;

@RestController
@RequestMapping("/api/social")
public class SocialLoginController {

    @Autowired
    private SocialLoginService socialLoginService;


}
