package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.side.model.TouristAttraction;
import side.side.model.TravelCourse;

import java.util.List;
import java.util.Optional;

public interface TravelCourseRepository  extends JpaRepository<TravelCourse, Long> {
    // 특정 contentTypeId에 따라 TravelCourse 리스트를 가져오는 메소드
    List<TravelCourse> findByContenttypeid(String contenttypeid);

    Optional<TravelCourse> findFirstByContentid(String contentid);
}
