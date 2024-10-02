package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import side.side.model.LocalBase;

@Repository
public interface LocalBasedRepository extends JpaRepository<LocalBase, Long> {

}
