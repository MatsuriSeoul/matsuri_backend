package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.LeisureSportsEvent;
import side.side.model.TravelCourse;

import java.util.List;

public interface LeisureSportsEventRepository extends JpaRepository<LeisureSportsEvent, Long> {
    // 특정 contentTypeId에 따라 TravelCourse 리스트를 가져오는 메소드
    List<LeisureSportsEvent> findByContenttypeid(String contenttypeid);
}
