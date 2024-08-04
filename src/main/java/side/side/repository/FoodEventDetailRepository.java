package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.FoodEventDetail;

public interface FoodEventDetailRepository extends JpaRepository<FoodEventDetail, String> {
}
