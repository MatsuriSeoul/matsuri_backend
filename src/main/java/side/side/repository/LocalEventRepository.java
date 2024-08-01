package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.LocalEvent;

public interface LocalEventRepository extends JpaRepository<LocalEvent, Long> {

}
