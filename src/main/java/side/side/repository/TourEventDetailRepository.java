package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.CulturalFacilityDetail;
import side.side.model.TourEventDetail;

public interface TourEventDetailRepository extends JpaRepository<TourEventDetail, Long> {
    // 특정 contentId에 따라 TourEventDetail 가져오는 메소드
    TourEventDetail findByContentid(String contentid);
}
