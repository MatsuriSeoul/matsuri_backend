package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.CulturalFacilityDetail;

public interface CulturalFacilityDetailRepository extends JpaRepository<CulturalFacilityDetail, Integer> {
    // 특정 contentId에 따라 CulturalFacilityDetail 가져오는 메소드
    CulturalFacilityDetail findByContentid(String contentid);
}
