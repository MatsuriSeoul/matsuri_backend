package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.side.model.LocalEvent;
import side.side.model.ShoppingEvent;
import side.side.model.TouristAttraction;

import java.util.List;
import java.util.Optional;

public interface ShoppingEventRepository extends JpaRepository<ShoppingEvent, Long> {
    // 특정 contentTypeId에 따라 TouristAttraction 리스트를 가져오는 메소드
    List<ShoppingEvent> findByContenttypeid(String contenttypeid);

    Optional<ShoppingEvent> findFirstByContentid(String contentid);

    //여행톡
    Optional<ShoppingEvent> findBycontentid(String contentid);
}
