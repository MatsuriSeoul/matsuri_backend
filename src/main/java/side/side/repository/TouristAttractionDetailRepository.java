package side.side.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.side.model.FoodEventDetail;
import side.side.model.TouristAttractionDetail;

import java.util.Optional;

public interface TouristAttractionDetailRepository extends JpaRepository<TouristAttractionDetail, Integer> {

    // 특정 contentId에 따라 TouristAttractionDetail을 가져오는 메소드
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    TouristAttractionDetail findByContentid(String contentid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TouristAttractionDetail t WHERE t.contentid = :contentid")
    Optional<TouristAttractionDetail> findByContentidForUpdate(@Param("contentid") String contentid);


    @Modifying
    @Query(value = "INSERT INTO tourist_attraction_detail (contentid, contenttypeid, booktour, createdtime, homepage, modifiedtime, tel, telname, title, firstimage, firstimage2, areacode, sigungucode, cat1, cat2, cat3, addr1, addr2, zipcode, mapx, mapy, mlevel, overview) " +
            "VALUES (:contentid, :contenttypeid, :booktour, :createdtime, :homepage, :modifiedtime, :tel, :telname, :title, :firstimage, :firstimage2, :areacode, :sigungucode, :cat1, :cat2, :cat3, :addr1, :addr2, :zipcode, :mapx, :mapy, :mlevel, :overview) " +
            "ON DUPLICATE KEY UPDATE " +
            "contenttypeid = VALUES(contenttypeid), " +
            "booktour = VALUES(booktour), " +
            "createdtime = VALUES(createdtime), " +
            "homepage = VALUES(homepage), " +
            "modifiedtime = VALUES(modifiedtime), " +
            "tel = VALUES(tel), " +
            "telname = VALUES(telname), " +
            "title = VALUES(title), " +
            "firstimage = VALUES(firstimage), " +
            "firstimage2 = VALUES(firstimage2), " +
            "" +
            "areacode = VALUES(areacode), " +
            "sigungucode = VALUES(sigungucode), " +
            "cat1 = VALUES(cat1), " +
            "cat2 = VALUES(cat2), " +
            "cat3 = VALUES(cat3), " +
            "addr1 = VALUES(addr1), " +
            "addr2 = VALUES(addr2), " +
            "zipcode = VALUES(zipcode), " +
            "mapx = VALUES(mapx), " +
            "mapy = VALUES(mapy), " +
            "mlevel = VALUES(mlevel), " +
            "overview = VALUES(overview)",
            nativeQuery = true)
    void upsertTouristAttractionDetail(@Param("contentid") String contentid,
                               @Param("contenttypeid") String contenttypeid,
                               @Param("booktour") String booktour,
                               @Param("createdtime") String createdtime,
                               @Param("homepage") String homepage,
                               @Param("modifiedtime") String modifiedtime,
                               @Param("tel") String tel,
                               @Param("telname") String telname,
                               @Param("title") String title,
                               @Param("firstimage") String firstimage,
                               @Param("firstimage2") String firstimage2,
                               @Param("areacode") String areacode,
                               @Param("sigungucode") String sigungucode,
                               @Param("cat1") String cat1,
                               @Param("cat2") String cat2,
                               @Param("cat3") String cat3,
                               @Param("addr1") String addr1,
                               @Param("addr2") String addr2,
                               @Param("zipcode") String zipcode,
                               @Param("mapx") String mapx,
                               @Param("mapy") String mapy,
                               @Param("mlevel") String mlevel,
                               @Param("overview") String overview);

}
