package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import side.side.model.TourEvent;

import java.util.List;

@Repository
public interface TourEventRepository extends JpaRepository<TourEvent, Long> {
    List<TourEvent> findByCat3In(String[] cat3Codes);

}
