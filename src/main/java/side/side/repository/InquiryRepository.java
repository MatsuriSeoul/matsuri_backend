package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.Inquiry;


import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findByUserId(Long userId);
}

