package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.CommentImage;

public interface CommentImageRepository extends JpaRepository<CommentImage, Long> {
}
