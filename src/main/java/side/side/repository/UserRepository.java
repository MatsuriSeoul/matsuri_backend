package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.UserInfo;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserInfo, Long> {
    boolean existsByUserId(String userId);
    boolean existsByUserEmail(String userEmail);
    Optional<UserInfo> findByUserId(String userId); // 사용자 ID로 사용자 정보 조회
  //  Optional<UserInfo> findByUserName(String userName); // 사용자 이름으로 사용자 정보 조회
}
