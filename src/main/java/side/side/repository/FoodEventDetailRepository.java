package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.FoodEventDetail;
import side.side.model.LocalEventDetail;

public interface FoodEventDetailRepository extends JpaRepository<FoodEventDetail, String> {
    // 특정 contentId에 따라 CulturalFacilityDetail 가져오는 메소드
    FoodEventDetail findByContentid(String contentid);
}
