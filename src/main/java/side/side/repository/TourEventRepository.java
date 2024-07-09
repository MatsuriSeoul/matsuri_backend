package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.TourEvent;

public interface TourEventRepository extends JpaRepository<TourEvent, Long> {

}
