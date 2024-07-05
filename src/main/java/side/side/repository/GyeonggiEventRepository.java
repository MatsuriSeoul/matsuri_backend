package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.side.model.GyeonggiEvent;

import java.util.List;

public interface GyeonggiEventRepository extends JpaRepository<GyeonggiEvent, Long> {
    @Query("SELECT e FROM GyeonggiEvent e WHERE " +
            "(:date IS NULL OR e.beginDe <= :date AND e.endDe >= :date) AND " +
            "(:category IS NULL OR e.categoryNm = :category)")
    List<GyeonggiEvent> findByCriteria(@Param("date") String date, @Param("category") String category);
}