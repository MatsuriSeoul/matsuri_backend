package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.TouristAttractionDetail;

public interface TouristAttractionDetailRepository extends JpaRepository<TouristAttractionDetail, Integer> {
    // 특정 contentId에 따라 TouristAttractionDetail을 가져오는 메소드
    TouristAttractionDetail findByContentid(String contentid);
}
