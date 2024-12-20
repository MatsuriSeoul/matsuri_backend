package side.side.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.side.model.FoodEvent;
import side.side.model.FoodEventDetail;
import side.side.model.LocalEventDetail;
import side.side.model.TourEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;

    public interface FoodEventRepository extends JpaRepository<FoodEvent, String> {

        // 특정 contentTypeId에 따라 TouristAttraction 리스트를 가져오는 메소드
        //유사한 여행지 추천
        List<FoodEvent> findByContenttypeid(String contenttypeid);

        Optional<FoodEvent> findFirstByContentid(String contentid);

        //여행톡
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        List<FoodEvent> findBycontentid(String contentid);

        // FoodEventRepository에 다음 메소드 추가
        @Query("SELECT t FROM FoodEvent t WHERE t.contentid = :contentid")
        Optional<FoodEvent> findByContentid(@Param("contentid") String contentid);


        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT t FROM FoodEvent t WHERE t.contentid = :contentid")
        Optional<FoodEvent> findByContentidForUpdate(@Param("contentid") String contentid);

        @Modifying
        @Query(value = "INSERT INTO food_event (contentid, title, addr1, firstimage,  mapx, mapy, contenttypeid) " +
                "VALUES (:contentid, :title, :addr1, :firstimage, :mapx, :mapy, :contenttypeid) " +
                "ON DUPLICATE KEY UPDATE " +
                "title = VALUES(title), " +
                "addr1 = VALUES(addr1), " +
                "firstimage = VALUES(firstimage), " +
                "mapx = VALUES(mapx), " +
                "mapy = VALUES(mapy), " +
                "contenttypeid = VALUES(contenttypeid)",
                nativeQuery = true)
        void upsertFoodEvent(@Param("contentid") String contentid,
                             @Param("title") String title,
                             @Param("addr1") String addr1,
                             @Param("firstimage") String firstimage,
                             @Param("mapx") String mapx,
                             @Param("mapy") String mapy,
                             @Param("contenttypeid") String contenttypeid);

        // addr1에서 부분 문자열 검색 및 contenttypeid로 필터링
        List<FoodEvent> findByAddr1ContainingAndContenttypeid(String addr1, String contenttypeid);


        // 문화시설 데이터 조회 (contenttypeid = 14)
        @Query("SELECT new map(t.contentid as contentid, t.title as title, t.firstimage as image, t.contenttypeid as contenttypeid) " +
                "FROM FoodEvent t WHERE t.contenttypeid = '39'")
        List<Map<String, Object>> findTopFoodEvents();

        @Query("SELECT new map(fe.contentid as contentid, fe.title as title, fe.firstimage as image, fe.contenttypeid as contenttypeid) " +
                "FROM FoodEvent fe WHERE fe.contentid = :contentid")
        List<Map<String, Object>> findTopFoodEventsByContentid(@Param("contentid") String contentid);

    }



