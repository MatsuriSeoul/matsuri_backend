package side.side.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import side.side.model.UserDetailView;
import side.side.repository.UserDetailViewRepository;
import side.side.repository.UserRepository;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

@Service
public class DetailViewService {

    @Autowired
    private UserDetailViewRepository userDetailViewRepository;

    public boolean addDetailView(Long userId, String contenttypeid, String contentid) {
        LocalDate today = LocalDate.now();

        // 오늘 해당 유저가 해당 컨텐츠를 조회했는지 확인
        Optional<UserDetailView> existingView = userDetailViewRepository.findViewByUserAndContentAndDate(userId, contenttypeid, contentid, today);

        // 이미 조회한 경우 조회수를 증가시키지 않음
        if (existingView.isPresent()) {
            return false;
        }

        // 조회수가 증가하는 경우 새로운 조회 기록을 추가
        UserDetailView userDetailView = new UserDetailView();
        userDetailView.setUserId(userId);
        userDetailView.setContenttypeid(contenttypeid);
        userDetailView.setContentid(contentid);
        userDetailView.setViewDate(today);
        userDetailViewRepository.save(userDetailView);

        return true;
    }

    public int getViewCount(String contenttypeid, String contentid) {
        return userDetailViewRepository.countByContenttypeidAndContentid(contenttypeid, contentid);
    }


}
