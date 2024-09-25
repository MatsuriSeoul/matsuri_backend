package side.side.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import side.side.config.JwtUtils;


@Service
public class SocialLoginService {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;



}
