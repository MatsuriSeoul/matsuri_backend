package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.side.model.SeoulEvent;

import java.util.List;

public interface SeoulEventRepository extends JpaRepository<SeoulEvent, Long> {
    @Query("SELECT e FROM SeoulEvent e WHERE " +
            "(:date IS NULL OR e.svcopnbgndt <= :date AND e.svcopnenddt >= :date) AND " +
            "(:category IS NULL OR e.minclassnm = :category)")
    List<SeoulEvent> findByCriteria(@Param("date") String date, @Param("category") String category);
}
