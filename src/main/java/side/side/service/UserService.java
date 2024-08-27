package side.side.service;

import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import side.side.model.UserInfo;
import side.side.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    // 사용자의 이메일로 id를 찾는 메소드
    public Optional<String> findUserIdByEmail(String email) {
        Optional<UserInfo> user = userRepository.findByUserEmail(email); // email을 이용해 userRepository에서 UserInfo를 찾는다
        return user.map(UserInfo::getUserId); // 찾으면 사용자 아이디 반환
    }

    // 사용자의 전화번호로 id를 찾는 메소드
    public Optional<String> findUserIdByPhone(String phone) {
        Optional<UserInfo> user = userRepository.findByUserPhone(phone); // phone을 이용해 userRepository에서 UserInfo를 찾는다
        return user.map(UserInfo::getUserId); // 찾으면 사용자 아이디 반환
    }

    public UserInfo saveUser(UserInfo userInfo) {
        if (userInfo.getRole() == null || userInfo.getRole().isEmpty()) {
            userInfo.setRole("USER"); // 기본 역할을 USER로 설정
        }
        return userRepository.save(userInfo);
    }

    public UserInfo saveAdmin(UserInfo userInfo) {
        userInfo.setRole("ADMIN"); // 관리자 역할로 설정
        return userRepository.save(userInfo);
    }

    public UserInfo findByUserName(String userName) {
        Optional<UserInfo> userInfo = Optional.ofNullable(userRepository.findByUserName(userName));
        return userInfo.orElse(null); // Optional에서 UserInfo로 변환
    }

    public UserInfo findByUserId(String userId) {
        Optional<UserInfo> userInfo = userRepository.findByUserId(userId);
        return userInfo.orElse(null); // Optional에서 UserInfo로 변환
    }

    // identifier와 type을 기반으로 비밀번호를 찾는 메소드
    public Optional<String> findPasswordByIdentifier(String identifier, String type) {
        Optional<UserInfo> user; // UserInfo 객체를 감싸는 Optional 선언
        switch (type) {
            case "id":
                user = userRepository.findByUserId(identifier); // identifier를 이용해 userRepository에서 UserInfo를 찾는다 이하 동일
                break;
            case "email":
                user = userRepository.findByUserEmail(identifier);
                break;
            case "phone":
                user = userRepository.findByUserPhone(identifier);
                break;
            default: // 다 아니면
                user = Optional.empty(); // 빈 값으로 설정
        }
        return user.map(UserInfo::getUserPassword); // user의 정보가 존재하면 그 비밀번호를 갖고 와서 반환함
    }

    public boolean checkUserIdExists(String userId) {
        return userRepository.existsByUserId(userId);
    }

    public boolean checkUserEmailExists(String userEmail) {
        return userRepository.existsByUserEmail(userEmail);
    }

    public Optional<UserInfo> getUserById(Long userId) {
        return userRepository.findById(userId);
    }
}

