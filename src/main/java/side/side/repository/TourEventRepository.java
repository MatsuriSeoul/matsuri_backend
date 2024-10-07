package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import side.side.model.TourEvent;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourEventRepository extends JpaRepository<TourEvent, Long> {

    List<TourEvent> findByCat3In(String[] cat3Codes);


    @Query(value = "SELECT * FROM tour_event WHERE addr1 LIKE CONCAT('%', :region, '%') ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<TourEvent> findRandomEventsByRegion(@Param("region") String region, @Param("limit") int limit);

    Optional<TourEvent> findBycontentid(String contentid);
}
