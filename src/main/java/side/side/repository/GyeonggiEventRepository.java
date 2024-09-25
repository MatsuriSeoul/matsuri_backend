package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.side.model.GyeonggiEvent;

import java.time.LocalDate;
import java.util.List;

public interface GyeonggiEventRepository extends JpaRepository<GyeonggiEvent, Long> {
    @Query("SELECT e FROM GyeonggiEvent e WHERE " +
            "(:date IS NULL OR e.beginDe <= :date AND e.endDe >= :date) AND " +
            "(:category IS NULL OR e.categoryNm = :category)")
    List<GyeonggiEvent> findByCriteria(@Param("date") String date, @Param("category") String category);

    // JPQL을 사용하여 제목과 이미지만 가져오는 쿼리
    // JQPL = 테이블을 대상으로 쿼리 하는 것이 아닌 엔티티 객체를 대상으로 쿼리함
    @Query("SELECT e.title, e.imageUrl FROM GyeonggiEvent e")
    List<Object[]> findTitlesAndImages();


    // category_nm 필드를 기준으로 카테고리 필터링
    List<GyeonggiEvent> findByCategoryNm(String categoryNm);

    // 경기도의 무료 행사 가져오기
    @Query(value = "SELECT * FROM gyeonggi_event WHERE partcpt_expn_info LIKE '%무료%' ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<GyeonggiEvent> findFreeEventsInGyeonggi(@Param("limit") int limit);

    // 경기도의 유료 행사 가져오기
    @Query(value = "SELECT * FROM gyeonggi_event WHERE (partcpt_expn_info NOT LIKE '%무료%' OR partcpt_expn_info IS NULL) ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<GyeonggiEvent> findPaidEventsInGyeonggi(@Param("limit") int limit);

    // 경기도의 개최 예정 or 중인 행사 가져오기
    @Query("SELECT e FROM GyeonggiEvent e WHERE e.beginDe <= :today AND e.endDe >= :today OR e.beginDe > :today")
    List<GyeonggiEvent> findScheduledEvents(@Param("today") String today);
}