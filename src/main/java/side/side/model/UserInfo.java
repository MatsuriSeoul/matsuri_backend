package side.side.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_info")
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "user_password", nullable = true) // 소셜 로그인 사용자의 경우 비밀번호가 없을 수 있음
    private String userPassword;

    @Column(name = "user_birthday")
    private String userBirthday;

    @Column(name = "user_phone")
    private String userPhone;

    @Column(name = "user_role")
    private String role; // 사용자, 관리자 구분

    @Column(name = "profile_image")
    private String profileImage;

    // 소셜 로그인 관련 필드 추가
    @Column(name = "social_provider")
    private String socialProvider; // 소셜 로그인 제공자 (ex: "google", "naver", "kakao")

    @Column(name = "social_id", unique = true)
    private String socialId; // 소셜 플랫폼에서 제공하는 고유 ID

}
