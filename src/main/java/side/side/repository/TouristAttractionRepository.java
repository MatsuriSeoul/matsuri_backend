package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.side.model.TouristAttraction;
import side.side.model.TravelCourse;

import java.util.List;
import java.util.Optional;

public interface TouristAttractionRepository extends JpaRepository<TouristAttraction, Long> {
    // 특정 contentTypeId에 따라 TouristAttraction 리스트를 가져오는 메소드
    List<TouristAttraction> findByContenttypeid(String contenttypeid);

    Optional<TouristAttraction> findFirstByContentid(String contentid);

    //여행톡
    Optional<TouristAttraction> findBycontentid(String contentid);

}
