package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.side.model.CulturalFacility;
import side.side.model.FoodEvent;
import side.side.model.LocalEvent;
import side.side.model.TouristAttraction;

import java.util.List;
import java.util.Optional;

public interface FoodEventRepository extends JpaRepository<FoodEvent, Long> {
    // 특정 contentTypeId에 따라 TouristAttraction 리스트를 가져오는 메소드
    //유사한 여행지 추천
    List<FoodEvent> findByContenttypeid(String contenttypeid);

    Optional<FoodEvent> findFirstByContentid(String contentid);

    //여행톡
    List<FoodEvent> findBycontentid(String contentid);

}
