package side.side.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.side.model.FoodEvent;
import side.side.model.TourEvent;
import side.side.model.TouristAttraction;
import side.side.model.TravelCourse;

import java.util.List;
import java.util.Optional;

public interface TouristAttractionRepository extends JpaRepository<TouristAttraction, Long> {
    // 특정 contentTypeId에 따라 TouristAttraction 리스트를 가져오는 메소드
    List<TouristAttraction> findByContenttypeid(String contenttypeid);

    Optional<TouristAttraction> findFirstByContentid(String contentid);

    //여행톡
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<TouristAttraction> findBycontentid(String contentid);

    @Query("SELECT t FROM TouristAttraction t WHERE t.contentid = :contentid")
    Optional<TouristAttraction> findByContentid(@Param("contentid") String contentid);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TouristAttraction t WHERE t.contentid = :contentid")
    Optional<TouristAttraction> findByContentidForUpdate(@Param("contentid") String contentid);

    @Modifying
    @Query(value = "INSERT INTO tourist_attraction (contentid, title, addr1, firstimage,  mapx, mapy, contenttypeid) " +
            "VALUES (:contentid, :title, :addr1, :firstimage, :mapx, :mapy, :contenttypeid) " +
            "ON DUPLICATE KEY UPDATE " +
            "title = VALUES(title), " +
            "addr1 = VALUES(addr1), " +
            "firstimage = VALUES(firstimage), " +
            "mapx = VALUES(mapx), " +
            "mapy = VALUES(mapy), " +
            "contenttypeid = VALUES(contenttypeid)",
            nativeQuery = true)
    void upsertTouristAttraction(@Param("contentid") String contentid,
                         @Param("title") String title,
                         @Param("addr1") String addr1,
                         @Param("firstimage") String firstimage,
                         @Param("mapx") String mapx,
                         @Param("mapy") String mapy,
                         @Param("contenttypeid") String contenttypeid);

    // addr1에서 부분 문자열 검색 및 contenttypeid로 필터링
    List<TouristAttraction> findByAddr1ContainingAndContenttypeid(String addr1, String contenttypeid);


}
