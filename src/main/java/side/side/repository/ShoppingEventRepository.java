package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.LocalEvent;
import side.side.model.ShoppingEvent;

import java.util.List;

public interface ShoppingEventRepository extends JpaRepository<ShoppingEvent, Long> {
    // 특정 contentTypeId에 따라 TouristAttraction 리스트를 가져오는 메소드
    List<ShoppingEvent> findByContenttypeid(String contenttypeid);
}
