package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.side.model.SeoulEvent;

import java.time.LocalDate;
import java.util.List;

public interface SeoulEventRepository extends JpaRepository<SeoulEvent, Long> {
    @Query("SELECT e FROM SeoulEvent e WHERE " +
            "(:date IS NULL OR e.svcopnbgndt <= :date AND e.svcopnenddt >= :date) AND " +
            "(:category IS NULL OR e.minclassnm = :category)")
    List<SeoulEvent> findByCriteria(@Param("date") String date, @Param("category") String category);

    // minclassnm 필드를 기준으로 카테고리 필터링
    List<SeoulEvent> findByMinclassnm(String minclassnm);
    SeoulEvent findBySvcid(String svcid);

    // 월, 카테고리를 기준으로 데이터 조회
    @Query("SELECT e FROM SeoulEvent e WHERE " +
            "(:category IS NULL OR e.minclassnm = :category) AND " +
            "(:month IS NULL OR SUBSTRING(e.svcopnbgndt, 6, 2) = :month)")
    List<SeoulEvent> findByCategoryAndMonth(@Param("month") String month, @Param("category") String category);

    // 카테고리에 맞는 모든 데이터를 조회
    @Query("SELECT e FROM SeoulEvent e WHERE e.minclassnm = :category")
    List<SeoulEvent> findByEventInCategory(@Param("category") String category);

    // 서울특별시의 무료 행사 가져오기
    @Query(value = "SELECT * FROM seoul_event WHERE PAYATNM = '무료' ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<SeoulEvent> findFreeEventsInSeoul(@Param("limit") int limit);

    // 서울특별시의 유료 행사 가져오기
    @Query(value = "SELECT * FROM seoul_event WHERE (PAYATNM != '무료' OR PAYATNM IS NULL) ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<SeoulEvent> findPaidEventsInSeoul(@Param("limit") int limit);

    // 서울에서 개최 예정 or 중인 행사 가져오기
    @Query("SELECT e FROM SeoulEvent e WHERE e.rcptbgndt <= :today AND e.rcptenddt >= :today OR e.rcptbgndt > :today")
    List<SeoulEvent> findScheduledEvents(@Param("today") String today);

}
