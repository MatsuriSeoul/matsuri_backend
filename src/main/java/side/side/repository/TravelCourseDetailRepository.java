package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.TourEventDetail;
import side.side.model.TravelCourseDetail;

public interface TravelCourseDetailRepository extends JpaRepository<TravelCourseDetail, Integer> {
    // 특정 contentId에 따라 TravelCourseDetail 가져오는 메소드
    TravelCourseDetail findByContentid(String contentid);
}
