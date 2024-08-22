package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.CulturalFacility;
import side.side.model.TouristAttraction;

import java.util.List;

public interface CulturalFacilityRepository extends JpaRepository<CulturalFacility, Long> {
    // 특정 contentTypeId에 따라 TouristAttraction 리스트를 가져오는 메소드
    List<CulturalFacility> findByContenttypeid(String contenttypeid);

}
