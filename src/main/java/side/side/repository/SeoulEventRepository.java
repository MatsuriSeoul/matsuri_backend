package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.SeoulEvent;

public interface SeoulEventRepository extends JpaRepository<SeoulEvent, Long> {
}