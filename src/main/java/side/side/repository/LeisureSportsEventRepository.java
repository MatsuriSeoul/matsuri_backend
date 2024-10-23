package side.side.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.side.model.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LeisureSportsEventRepository extends JpaRepository<LeisureSportsEvent, Long> {
    // 특정 contentTypeId에 따라 TravelCourse 리스트를 가져오는 메소드
    List<LeisureSportsEvent> findByContenttypeid(String contenttypeid);

    Optional<LeisureSportsEvent> findFirstByContentid(String contentid);

    //여행톡
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<LeisureSportsEvent> findBycontentid(String contentid);

    // FoodEventRepository에 다음 메소드 추가
    @Query("SELECT t FROM LeisureSportsEvent t WHERE t.contentid = :contentid")
    Optional<LeisureSportsEvent> findByContentid(@Param("contentid") String contentid);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM LeisureSportsEvent t WHERE t.contentid = :contentid")
    Optional<LeisureSportsEvent> findByContentidForUpdate(@Param("contentid") String contentid);

    @Modifying
    @Query(value = "INSERT INTO leisure_sports_event (contentid, title, addr1, eventstartdate, eventenddate, firstimage, cat1, cat2, cat3, contenttypeid) " +
            "VALUES (:contentid, :title, :addr1, :eventstartdate, :eventenddate, :firstimage, :cat1, :cat2, :cat3, :contenttypeid) " +
            "ON DUPLICATE KEY UPDATE " +
            "title = VALUES(title), " +
            "addr1 = VALUES(addr1), " +
            "eventstartdate = VALUES(eventstartdate), " +
            "eventenddate = VALUES(eventenddate), " +
            "firstimage = VALUES(firstimage), " +
            "cat1 = VALUES(cat1), " +
            "cat2 = VALUES(cat2), " +
            "cat3 = VALUES(cat3), " +
            "contenttypeid = VALUES(contenttypeid)",
            nativeQuery = true)
    void upsertLeisureSportsEvent(@Param("contentid") String contentid,
                                  @Param("title") String title,
                                  @Param("addr1") String addr1,
                                  @Param("eventstartdate") String eventstartdate,
                                  @Param("eventenddate") String eventenddate,
                                  @Param("firstimage") String firstimage,
                                  @Param("cat1") String cat1,
                                  @Param("cat2") String cat2,
                                  @Param("cat3") String cat3,
                                  @Param("contenttypeid") String contenttypeid);

    // addr1에서 부분 문자열 검색 및 contenttypeid로 필터링
    List<LeisureSportsEvent> findByAddr1ContainingAndContenttypeid(String addr1, String contenttypeid);

    // 문화시설 데이터 조회 (contenttypeid = 14)
    @Query("SELECT new map(t.contentid as contentid, t.title as title, t.firstimage as image, t.contenttypeid as contenttypeid) " +
            "FROM LeisureSportsEvent t WHERE t.contenttypeid = '28'")
    List<Map<String, Object>> findTopLeisureSports();

    @Query("SELECT new map(le.contentid as contentid, le.title as title, le.firstimage as image, le.contenttypeid as contenttypeid) " +
            "FROM LeisureSportsEvent le WHERE le.contentid = :contentid")
    List<Map<String, Object>> findTopLeisureSportsEventsByContentid(@Param("contentid") String contentid);



}
