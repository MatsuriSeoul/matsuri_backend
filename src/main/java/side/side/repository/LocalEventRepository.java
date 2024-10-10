package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.side.model.CulturalFacility;
import side.side.model.LocalEvent;
import side.side.model.ShoppingEvent;
import side.side.model.TouristAttraction;

import java.util.List;
import java.util.Optional;

public interface LocalEventRepository extends JpaRepository<LocalEvent, Long> {
    // 특정 contentTypeId에 따라 TouristAttraction 리스트를 가져오는 메소드
    List<LocalEvent> findByContenttypeid(String contenttypeid);

    Optional<LocalEvent> findFirstByContentid(String contentid);

    //여행톡
    Optional<LocalEvent> findBycontentid(String contentid);

}
