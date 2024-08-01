package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.TouristAttraction;

public interface TouristAttractionRepository extends JpaRepository<TouristAttraction, Long> {
}
