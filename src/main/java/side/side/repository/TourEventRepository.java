package side.side.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import side.side.model.TourEvent;


import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface TourEventRepository extends JpaRepository<TourEvent, Long> {

    List<TourEvent> findByCat3In(String[] cat3Codes);

    Optional<TourEvent> findFirstByContentid(String contentid);

    @Query(value = "SELECT * FROM tour_event WHERE addr1 LIKE CONCAT('%', :region, '%') ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<TourEvent> findRandomEventsByRegion(@Param("region") String region, @Param("limit") int limit);

    //여행톡
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<TourEvent> findBycontentid(String contentid);

    // TourEventRepository에 다음 메소드 추가
    @Query("SELECT t FROM TourEvent t WHERE t.contentid = :contentid")
    Optional<TourEvent> findByContentid(@Param("contentid") String contentid);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TourEvent t WHERE t.contentid = :contentid")
    Optional<TourEvent> findByContentidForUpdate(@Param("contentid") String contentid);


    //유사한 여행지 추천
    List<TourEvent> findByContenttypeid(String contenttypeid);

    @Query(value = "SELECT * FROM tour_event t WHERE (:month = '' OR SUBSTRING(t.eventstartdate, 5, 2) = :month) AND (:region IS NULL OR t.addr1 LIKE %:region%)", nativeQuery = true)
    List<TourEvent> findByMonthAndRegion(@Param("month") String month, @Param("region") String region);

    @Modifying
    @Query(value = "INSERT INTO tour_event (contentid, title, addr1, eventstartdate, eventenddate, firstimage, cat1, cat2, cat3, mapx, mapy, contenttypeid) " +
            "VALUES (:contentid, :title, :addr1, :eventstartdate, :eventenddate, :firstimage, :cat1, :cat2, :cat3, :mapx, :mapy, :contenttypeid) " +
            "ON DUPLICATE KEY UPDATE " +
            "title = VALUES(title), " +
            "addr1 = VALUES(addr1), " +
            "eventstartdate = VALUES(eventstartdate), " +
            "eventenddate = VALUES(eventenddate), " +
            "firstimage = VALUES(firstimage), " +
            "cat1 = VALUES(cat1), " +
            "cat2 = VALUES(cat2), " +
            "cat3 = VALUES(cat3), " +
            "mapx = VALUES(mapx), " +
            "mapy = VALUES(mapy), " +
            "contenttypeid = VALUES(contenttypeid)",
            nativeQuery = true)
    void upsertTourEvent(@Param("contentid") String contentid,
                         @Param("title") String title,
                         @Param("addr1") String addr1,
                         @Param("eventstartdate") String eventstartdate,
                         @Param("eventenddate") String eventenddate,
                         @Param("firstimage") String firstimage,
                         @Param("cat1") String cat1,
                         @Param("cat2") String cat2,
                         @Param("cat3") String cat3,
                         @Param("mapx") String mapx,
                         @Param("mapy") String mapy,
                         @Param("contenttypeid") String contenttypeid);


    // addr1에서 부분 문자열 검색 및 contenttypeid로 필터링
    List<TourEvent> findByAddr1ContainingAndContenttypeid(String addr1, String contenttypeid);

    @Query("SELECT new map(t.contentid as contentid, t.title as title, t.firstimage as image, t.contenttypeid as contenttypeid) " +
            "FROM TourEvent t WHERE t.contenttypeid = '15'")
    List<Map<String, Object>> findTopEvents();
}

