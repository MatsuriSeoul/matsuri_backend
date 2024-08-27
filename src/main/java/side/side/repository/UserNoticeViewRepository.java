package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.side.model.Notice;
import side.side.model.UserInfo;
import side.side.model.UserNoticeView;

import java.time.LocalDate;

public interface UserNoticeViewRepository extends JpaRepository<UserNoticeView,Long> {
    boolean existsByUserAndNoticeAndViewDate(UserInfo user, Notice notice, LocalDate viewDate);
    void deleteByNotice(Notice notice);
}
