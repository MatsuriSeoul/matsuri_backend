package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.NoticeFile;

public interface NoticeFileRepository extends JpaRepository<NoticeFile, Long> {
}
