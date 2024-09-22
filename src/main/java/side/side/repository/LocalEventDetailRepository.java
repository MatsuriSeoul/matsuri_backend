package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.CulturalFacilityDetail;
import side.side.model.LocalEventDetail;

public interface LocalEventDetailRepository extends JpaRepository<LocalEventDetail, Long> {
    // 특정 contentId에 따라 CulturalFacilityDetail 가져오는 메소드
    LocalEventDetail findByContentid(String contentid);
}
