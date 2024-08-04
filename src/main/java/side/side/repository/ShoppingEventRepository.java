package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.ShoppingEvent;

public interface ShoppingEventRepository extends JpaRepository<ShoppingEvent, Long> {
}
