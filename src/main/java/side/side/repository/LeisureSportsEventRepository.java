package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.side.model.CulturalFacility;
import side.side.model.LeisureSportsEvent;
import side.side.model.TouristAttraction;
import side.side.model.TravelCourse;

import java.util.List;
import java.util.Optional;

public interface LeisureSportsEventRepository extends JpaRepository<LeisureSportsEvent, Long> {
    // 특정 contentTypeId에 따라 TravelCourse 리스트를 가져오는 메소드
    List<LeisureSportsEvent> findByContenttypeid(String contenttypeid);

    Optional<LeisureSportsEvent> findFirstByContentid(String contentid);

    //여행톡
    Optional<LeisureSportsEvent> findBycontentid(String contentid);
}
