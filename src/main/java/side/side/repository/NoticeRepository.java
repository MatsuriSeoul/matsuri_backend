package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
