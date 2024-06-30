package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.GyeonggiEvent;

public interface GyeonggiEventRepository extends JpaRepository<GyeonggiEvent, Long> {
}