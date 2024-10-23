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

public interface LocalEventRepository extends JpaRepository<LocalEvent, Long> {
    // 특정 contentTypeId에 따라 TouristAttraction 리스트를 가져오는 메소드
    List<LocalEvent> findByContenttypeid(String contenttypeid);

    Optional<LocalEvent> findFirstByContentid(String contentid);

    //여행톡
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<LocalEvent> findBycontentid(String contentid);

    // FoodEventRepository에 다음 메소드 추가
    @Query("SELECT t FROM LocalEvent t WHERE t.contentid = :contentid")
    Optional<LocalEvent> findByContentid(@Param("contentid") String contentid);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM LocalEvent t WHERE t.contentid = :contentid")
    Optional<LocalEvent> findByContentidForUpdate(@Param("contentid") String contentid);

    @Modifying
    @Query(value = "INSERT INTO local_event (contentid, title, addr1, firstimage, begin_de, end_de, cat1, cat2, cat3, eventstartdate, eventenddate, image_url, region_nm, contenttypeid, mapx, mapy) " +
            "VALUES (:contentid, :title, :addr1, :firstimage, :begin_de, :end_de, :cat1, :cat2, :cat3, :eventstartdate, :eventenddate, :image_url, :region_nm, :contenttypeid, :mapx, :mapy) " +
            "ON DUPLICATE KEY UPDATE " +
            "title = VALUES(title), " +
            "addr1 = VALUES(addr1), " +
            "firstimage = VALUES(firstimage), " +
            "begin_de = VALUES(begin_de), " +
            "end_de = VALUES(end_de), " +
            "cat1 = VALUES(cat1), " +
            "cat2 = VALUES(cat2), " +
            "cat3 = VALUES(cat3), " +
            "eventstartdate = VALUES(eventstartdate), " +
            "eventenddate = VALUES(eventenddate), " +
            "image_url = VALUES(image_url), " +
            "region_nm = VALUES(region_nm), " +
            "contenttypeid = VALUES(contenttypeid)," +
            "mapx = VALUES(mapx), " +
            "mapx = VALUES(mapy)",
            nativeQuery = true)
    void upsertLocalEvent(@Param("contentid") String contentid,
                          @Param("title") String title,
                          @Param("addr1") String addr1,
                          @Param("firstimage") String firstimage,
                          @Param("begin_de") String begin_de,
                          @Param("end_de") String end_de,
                          @Param("cat1") String cat1,
                          @Param("cat2") String cat2,
                          @Param("cat3") String cat3,
                          @Param("eventstartdate") String eventstartdate,
                          @Param("eventenddate") String eventenddate,
                          @Param("image_url") String image_url,
                          @Param("region_nm") String region_nm,
                          @Param("contenttypeid") String contenttypeid,
                          @Param("mapx") String mapx,
                          @Param("mapy") String mapy);

    // addr1에서 부분 문자열 검색 및 contenttypeid로 필터링
    List<LocalEvent> findByAddr1ContainingAndContenttypeid(String addr1, String contenttypeid);

    // 숙박 데이터 조회
    @Query("SELECT new map(t.contentid as contentid, t.title as title, t.firstimage as image, t.contenttypeid as contenttypeid) " +
            "FROM LocalEvent t WHERE t.contenttypeid = '32'")
    List<Map<String, Object>> findTopAccommodations();

    @Query("SELECT new map(le.contentid as contentid, le.title as title, le.firstimage as image, le.contenttypeid as contenttypeid) " +
            "FROM LocalEvent le WHERE le.contentid = :contentid")
    List<Map<String, Object>> findTopLocalEventsByContentid(@Param("contentid") String contentid);

}
