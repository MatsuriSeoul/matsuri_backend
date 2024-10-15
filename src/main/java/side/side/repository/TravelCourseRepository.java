package side.side.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.side.model.*;
import side.side.model.TravelCourse;

import java.util.List;
import java.util.Optional;

public interface TravelCourseRepository  extends JpaRepository<TravelCourse, Long> {
    // 특정 contentTypeId에 따라 TravelCourse 리스트를 가져오는 메소드
    List<TravelCourse> findByContenttypeid(String contenttypeid);

    Optional<TravelCourse> findFirstByContentid(String contentid);

    //여행톡
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<TravelCourse> findBycontentid(String contentid);

    // TravelCourseRepository에 다음 메소드 추가
    @Query("SELECT t FROM TravelCourse t WHERE t.contentid = :contentid")
    Optional<TravelCourse> findByContentid(@Param("contentid") String contentid);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TravelCourse t WHERE t.contentid = :contentid")
    Optional<TravelCourse> findByContentidForUpdate(@Param("contentid") String contentid);

    @Modifying
    @Query(value = "INSERT INTO travel_course (contentid, title, addr1,  mapx, mapy, contenttypeid) " +
            "VALUES (:contentid, :title, :addr1, :mapx, :mapy, :contenttypeid) " +
            "ON DUPLICATE KEY UPDATE " +
            "title = VALUES(title), " +
            "addr1 = VALUES(addr1), " +
            "mapx = VALUES(mapx), " +
            "mapy = VALUES(mapy), " +
            "contenttypeid = VALUES(contenttypeid)",
            nativeQuery = true)
    void upsertTravelCourse(@Param("contentid") String contentid,
                            @Param("title") String title,
                            @Param("addr1") String addr1,
                            @Param("mapx") String mapx,
                            @Param("mapy") String mapy,
                            @Param("contenttypeid") String contenttypeid);
    // addr1에서 부분 문자열 검색 및 contenttypeid로 필터링
    List<TravelCourse> findByAddr1ContainingAndContenttypeid(String addr1, String contenttypeid);

}
