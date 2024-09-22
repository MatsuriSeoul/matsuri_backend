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

    @Column(name = "user_password")
    private String userPassword;

    @Column(name = "user_birthday")
    private String userBirthday;

    @Column(name = "user_phone")
    private String userPhone;

    @Column(name = "user_role")
    private String role; // 사용자, 관리자 구분

    @Column(name = "profile_image")
    private String profileImage;

}
