package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.FoodEvent;

public interface FoodEventRepository extends JpaRepository<FoodEvent, Long> {
}
