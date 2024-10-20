package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import side.side.model.UserClickLog;

import java.util.List;

public interface UserClickLogRepository extends JpaRepository<UserClickLog, Long> {

    // 특정 사용자가 가장 많이 조회한 contenttypeid와 조회 횟수를 반환하는 쿼리
    @Query("SELECT u.contenttypeid, COUNT(u) as cnt " +
            "FROM UserClickLog u " +
            "WHERE u.user.id = :userId " +
            "GROUP BY u.contenttypeid " +
            "ORDER BY cnt DESC")
    List<Object[]> findTopCategoryByUserId(Long userId);

}
