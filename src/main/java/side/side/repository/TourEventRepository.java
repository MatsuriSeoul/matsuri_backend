package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import side.side.model.TourEvent;
import side.side.model.TouristAttraction;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourEventRepository extends JpaRepository<TourEvent, Long> {
    List<TourEvent> findByCat3In(String[] cat3Codes);

    Optional<TourEvent> findFirstByContentid(String contentid);

    @Query(value = "SELECT * FROM tour_event WHERE addr1 LIKE CONCAT('%', :region, '%') ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<TourEvent> findRandomEventsByRegion(@Param("region") String region, @Param("limit") int limit);

    //유사한 여행지 추천
    List<TourEvent> findByContenttypeid(String contenttypeid);
}
