package side.side.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.side.model.ShoppingEvent;


import java.util.List;
import java.util.Optional;

public interface ShoppingEventRepository extends JpaRepository<ShoppingEvent, Long> {

    // 특정 contentTypeId에 따라 TouristAttraction 리스트를 가져오는 메소드
    List<ShoppingEvent> findByContenttypeid(String contenttypeid);

    Optional<ShoppingEvent> findFirstByContentid(String contentid);

    //여행톡
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<ShoppingEvent> findBycontentid(String contentid);

    // ShoppingEventRepository에 다음 메소드 추가
    @Query("SELECT t FROM ShoppingEvent t WHERE t.contentid = :contentid")
    Optional<ShoppingEvent> findByContentid(@Param("contentid") String contentid);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM ShoppingEvent t WHERE t.contentid = :contentid")
    Optional<ShoppingEvent> findByContentidForUpdate(@Param("contentid") String contentid);

    @Modifying
    @Query(value = "INSERT INTO shopping_event (contentid, title, addr1, firstimage,  mapx, mapy, contenttypeid) " +
            "VALUES (:contentid, :title, :addr1, :firstimage, :mapx, :mapy, :contenttypeid) " +
            "ON DUPLICATE KEY UPDATE " +
            "title = VALUES(title), " +
            "addr1 = VALUES(addr1), " +
            "firstimage = VALUES(firstimage), " +
            "mapx = VALUES(mapx), " +
            "mapy = VALUES(mapy), " +
            "contenttypeid = VALUES(contenttypeid)",
            nativeQuery = true)
    void upsertShoppingEvent(@Param("contentid") String contentid,
                             @Param("title") String title,
                             @Param("addr1") String addr1,
                             @Param("firstimage") String firstimage,
                             @Param("mapx") String mapx,
                             @Param("mapy") String mapy,
                             @Param("contenttypeid") String contenttypeid);


}
