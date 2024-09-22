package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.Inquiry;


import java.util.List;
import java.util.Optional;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findByUserId(Long userId);
    Optional<Inquiry> findByIdAndUserId(Long id, Long userId);  // 사용자별 문의 수정/삭제를 위한 조회
}
