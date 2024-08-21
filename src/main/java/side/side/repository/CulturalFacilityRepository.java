package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.CulturalFacility;
import side.side.model.TouristAttraction;

import java.util.List;

public interface CulturalFacilityRepository extends JpaRepository<CulturalFacility, Long> {
    List<CulturalFacility> findByContenttypeid(String contenttypeid);
}
