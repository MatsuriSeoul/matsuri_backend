package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.UserInfo;

public interface UserRepository extends JpaRepository<UserInfo, Long> {
    boolean existsByUserId(String userId);
    boolean existsByUserEmail(String userEmail);
}
