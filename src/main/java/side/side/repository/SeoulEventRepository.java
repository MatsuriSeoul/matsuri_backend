package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.side.model.SeoulEvent;

import java.util.List;

public interface SeoulEventRepository extends JpaRepository<SeoulEvent, Long> {
    @Query("SELECT e FROM SeoulEvent e WHERE " +
            "(:date IS NULL OR e.svcopnbgndt <= :date AND e.svcopnenddt >= :date) AND " +
            "(:category IS NULL OR e.minclassnm = :category)")
    List<SeoulEvent> findByCriteria(@Param("date") String date, @Param("category") String category);

    // minclassnm 필드를 기준으로 카테고리 필터링
    List<SeoulEvent> findByMinclassnm(String minclassnm);

    SeoulEvent findBySvcid(String svcid);

    // 카테고리와 날짜 범위로 필터링
    @Query("SELECT s FROM SeoulEvent s WHERE s.minclassnm = :category AND s.rcptbgndt >= :startDate AND s.rcptenddt <= :endDate")
    List<SeoulEvent> findByCategoryAndDateRange(@Param("category") String category,
                                                @Param("startDate") String startDate,
                                                @Param("endDate") String endDate);
}
