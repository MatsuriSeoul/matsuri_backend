package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.Response;

public interface ResponseRepository extends JpaRepository<Response, Long> {
}

