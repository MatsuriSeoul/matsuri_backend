package side.side.repository;

import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.UserInfo;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserInfo, Long> {
    boolean existsByUserId(String userId);  //  Id 중복 검사
    Optional<UserInfo> findByUserId(String userId); // 사용자 ID로 사용자 정보 조회
    Optional<UserInfo> findByUserPhone(String phone); // 폰 번호로 id 찾기
    Optional<UserInfo> findByUserEmail(String email); // 이메일로 사용자 정보 조회
    UserInfo findByUserName(String userName);
    boolean existsByUserEmail(String userEmail);  // 이메일 중복 검사
    UserInfo findBySocialIdAndSocialProvider(String socialId, String Provider);
}
