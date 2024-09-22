package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.side.model.FoodEvent;
import side.side.model.LocalEvent;

import java.util.List;

public interface FoodEventRepository extends JpaRepository<FoodEvent, Long> {
    // 특정 contentTypeId에 따라 TouristAttraction 리스트를 가져오는 메소드
    List<FoodEvent> findByContenttypeid(String contenttypeid);

}
