package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.side.model.PasswordHistory;

import java.time.LocalDateTime;
import java.util.List;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {

    @Query(
            "SELECT ph FROM PasswordHistory ph " +
                    "WHERE ph.user.id = :userId " +
                    "AND ph.createdAt >= :threeMonthsAgo"
    )
    List<PasswordHistory> findRecentPasswords(@Param("userId") Long userId, @Param("threeMonthsAgo") LocalDateTime threeMonthsAgo);

    @Modifying
    @Query(
            "DELETE FROM PasswordHistory ph " +
                    "WHERE ph.createdAt < :threeMonthsAgo")
    void deleteOldPasswordHistory(@Param("threeMonthsAgo") LocalDateTime threeMonthsAgo);

}
