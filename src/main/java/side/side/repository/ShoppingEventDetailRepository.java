package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.LocalEventDetail;
import side.side.model.ShoppingEventDetail;

public interface ShoppingEventDetailRepository extends JpaRepository<ShoppingEventDetail, Integer> {
    // 특정 contentId에 따라 CulturalFacilityDetail 가져오는 메소드
    ShoppingEventDetail findByContentid(String contentid);
}
