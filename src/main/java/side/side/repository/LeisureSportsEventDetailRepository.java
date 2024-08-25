package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.LeisureSportsEventDetail;
import side.side.model.TravelCourseDetail;

public interface LeisureSportsEventDetailRepository extends JpaRepository<LeisureSportsEventDetail, Long> {
    // 특정 contentId에 따라 TravelCourseDetail 가져오는 메소드
    LeisureSportsEventDetail findByContentid(String contentid);
}
