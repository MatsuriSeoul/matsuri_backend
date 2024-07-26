package side.side.response;//토큰을 응답하기 위한 로그인

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String userName;
    private String userId;

    public LoginResponse(String token) {
    }
}