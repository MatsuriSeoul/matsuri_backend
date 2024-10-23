package side.side.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.side.model.CulturalFacility;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CulturalFacilityRepository extends JpaRepository<CulturalFacility, Long> {
    // 특정 contentTypeId에 따라 TouristAttraction 리스트를 가져오는 메소드
    List<CulturalFacility> findByContenttypeid(String contenttypeid);

    Optional<CulturalFacility> findFirstByContentid(String contentid);

    //여행톡
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<CulturalFacility> findBycontentid(String contentid);

    @Query("SELECT t FROM CulturalFacility t WHERE t.contentid = :contentid")
    Optional<CulturalFacility> findByContentid(@Param("contentid") String contentid);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM CulturalFacility t WHERE t.contentid = :contentid")
    Optional<CulturalFacility> findByContentidForUpdate(@Param("contentid") String contentid);

    @Modifying
    @Query(value = "INSERT INTO cultural_facility (contentid, title, addr1, firstimage, mapx, mapy, contenttypeid, tel, overview) " +
            "VALUES (:contentid, :title, :addr1, :firstimage, :mapx, :mapy, :contenttypeid, :tel, :overview) " +
            "ON DUPLICATE KEY UPDATE " +
            "title = VALUES(title), " +
            "addr1 = VALUES(addr1), " +
            "firstimage = VALUES(firstimage), " +
            "mapx = VALUES(mapx), " +
            "mapy = VALUES(mapy), " +
            "tel = VALUES(tel), " +
            "overview = VALUES(overview), " +
            "contenttypeid = VALUES(contenttypeid)",
            nativeQuery = true)
    void upsertCulFacilityEvent(
            @Param("contentid") String contentid,
            @Param("title") String title,
            @Param("addr1") String addr1,
            @Param("firstimage") String firstimage,
            @Param("mapx") String mapx,
            @Param("mapy") String mapy,
            @Param("contenttypeid") String contenttypeid,
            @Param("tel") String tel,
            @Param("overview") String overview);

    // addr1에서 부분 문자열 검색 및 contenttypeid로 필터링
    List<CulturalFacility> findByAddr1ContainingAndContenttypeid(String addr1, String contenttypeid);

    // 문화시설 데이터 조회 (contenttypeid = 14)
    @Query("SELECT new map(t.contentid as contentid, t.title as title, t.firstimage as image, t.contenttypeid as contenttypeid) " +
            "FROM CulturalFacility t WHERE t.contenttypeid = '14'")
    List<Map<String, Object>> findTopCulturalFacilities();

    @Query("SELECT new map(cf.contentid as contentid, cf.title as title, cf.firstimage as image, cf.contenttypeid as contenttypeid) " +
            "FROM CulturalFacility cf WHERE cf.contentid = :contentid")
    List<Map<String, Object>> findTopCulturalFacilitiesByContentid(@Param("contentid") String contentid);


}
