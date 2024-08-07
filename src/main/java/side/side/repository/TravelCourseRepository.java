package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.TravelCourse;

public interface TravelCourseRepository  extends JpaRepository<TravelCourse, Long> {
}
