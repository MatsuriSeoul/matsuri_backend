package side.side.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import side.side.model.UserClickLog;
import side.side.model.UserInfo;
import side.side.repository.UserClickLogRepository;
import side.side.repository.UserRepository;

@Service
public class UserClickLogService {

    @Autowired
    private UserClickLogRepository userClickLogRepository;

    @Autowired
    private UserRepository userRepository;

    // 사용자 클릭 로그를 저장하는 메서드
    public void logUserClick(Long userId, String contentid, String contenttypeid) {
        // 사용자 정보 가져오기
        UserInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 새로운 클릭 로그 생성
        UserClickLog log = new UserClickLog();
        log.setUser(user); // 사용자 객체를 설정
        log.setContentid(contentid);
        log.setContenttypeid(contenttypeid);

        // 클릭 로그 DB에 저장
        userClickLogRepository.save(log);
    }
}
