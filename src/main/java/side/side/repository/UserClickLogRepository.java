package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.UserClickLog;

public interface UserClickLogRepository extends JpaRepository<UserClickLog, Long> {
}
