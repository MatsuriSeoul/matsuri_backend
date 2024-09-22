package side.side.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import side.side.config.SecurityConfig;
import side.side.model.PasswordHistory;
import side.side.model.UserInfo;
import side.side.repository.PasswordHistoryRepository;
import side.side.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${twilio.account_sid}")
    private String accountSid;

    @Value("${twilio.auth_token}")
    private String authToken;

    @Value("${twilio.phone_number}")
    private String fromPhoneNumber;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private static final int VERIFICATION_CODE_LENGTH = 6;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordHistoryRepository passwordHistoryRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SecurityConfig.PasswordEncoder passwordEncoder;



    private ConcurrentHashMap<String, VerificationCode> verificationCodeMap = new ConcurrentHashMap<>();


    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSid, authToken);
    }

    ///////////////////////////////  회원가입 시 이메일, 전화번호 인증번호 전송 ///////////////////////////////

    public boolean sendVerificationCodeByEmail(String email) {
        String code = generateVerificationCode();
        VerificationCode verificationCode = new VerificationCode(code, LocalDateTime.now().plusMinutes(5));
        verificationCodeMap.put(email, verificationCode);

        try {
            sendEmailVerificationCode(email, code);
            log.info("Verification code sent successfully to email: {}", email);
            return true;
        } catch (Exception e) {
            log.error("Failed to send verification code via email: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean sendVerificationCodeByPhone(String phone) {
        String code = generateVerificationCode();
        VerificationCode verificationCode = new VerificationCode(code, LocalDateTime.now().plusMinutes(5));
        verificationCodeMap.put(phone, verificationCode);

        try {
            String internationalPhone = formatPhoneNumber(phone);
            Message.creator(new PhoneNumber(internationalPhone), new PhoneNumber(fromPhoneNumber),
                    "인증번호 : " + code + " 유효 시간은 5분입니다.").create();
            log.info("Verification code sent successfully to phone: {}", phone);
            return true;
        } catch (Exception e) {
            log.error("Failed to send verification code via phone: {}", e.getMessage(), e);
            return false;
        }
    }
    ///////////////////////////////  회원가입 시 이메일, 전화번호 인증번호 전송 ///////////////////////////////

    ///////////////////////////////  비밀번호 찾기 로직 ///////////////////////////////

    // 이메일로 인증번호 생성 및 발송 (비밀번호 찾기)
    public boolean sendVerificationCodeByEmailForPassword(String userId, String email) {
        log.info("Attempting to send verification code via email for userId: {} and email: {}", userId, email);

        Optional<UserInfo> user = userRepository.findByUserIdAndUserEmail(userId, email);
        if (user.isEmpty()) {
            log.error("Email verification failed: email not associated with userId: {}", userId);
            return false;  // 이메일이 유효하지 않으면 전송하지 않음
        }

        String code = generateVerificationCode();
        VerificationCode verificationCode = new VerificationCode(code, LocalDateTime.now().plusMinutes(5));
        verificationCodeMap.put(email, verificationCode);  // email로 저장

        try {
            sendEmailVerificationCode(email, code);
            log.info("Verification code sent successfully to email: {}", email);
            return true;
        } catch (Exception e) {
            log.error("Failed to send verification code via email: {}", e.getMessage(), e);
            return false;
        }
    }

    // 전화번호로 인증번호 생성 및 발송 (비밀번호 찾기)
    public boolean sendVerificationCodeByPhoneForPassword(String userId, String phone) {
        log.info("Attempting to send verification code via phone for userId: {} and phone: {}", userId, phone);

        Optional<UserInfo> user = userRepository.findByUserIdAndUserPhone(userId, phone);
        if (user.isEmpty()) {
            log.error("Phone verification failed: phone number not associated with userId: {}", userId);
            return false;  // 전화번호가 유효하지 않으면 전송하지 않음
        }

        String code = generateVerificationCode();
        VerificationCode verificationCode = new VerificationCode(code, LocalDateTime.now().plusMinutes(5));
        verificationCodeMap.put(phone, verificationCode);  // userId + phone으로 저장

        try {
            String internationalPhone = formatPhoneNumber(phone);
            Message.creator(new PhoneNumber(internationalPhone), new PhoneNumber(fromPhoneNumber),
                    "인증번호 : " + code + " 유효 시간은 5분입니다.").create();
            log.info("Verification code sent successfully to phone: {}", phone);
            return true;
        } catch (Exception e) {
            log.error("Failed to send verification code via phone: {}", e.getMessage(), e);
            return false;
        }
    }
    ///////////////////////////////  비밀번호 찾기 로직 ///////////////////////////////


    ///////////////////////////////  아이디 찾기 로직 ///////////////////////////////

    // 전화번호로 인증번호 생성 및 발송 (아이디 찾기 로직 : 이름 + 전화번호 조합)
    public boolean sendVerificationCodeByPhoneForUserName(String userName, String phone) {
        log.info("Attempting to send verification code via phone for userName: {} and phone: {}", userName, phone);

        Optional<UserInfo> user = userRepository.findByUserNameAndUserPhone(userName, phone);
        if (user.isEmpty()) {
            log.error("Phone verification failed: phone number not associated with userName: {}", userName);
            return false;
        }

        String code = generateVerificationCode();
        VerificationCode verificationCode = new VerificationCode(code, LocalDateTime.now().plusMinutes(5));
        verificationCodeMap.put(phone, verificationCode);  // phone으로 저장

        try {
            String internationalPhone = formatPhoneNumber(phone);
            Message.creator(new PhoneNumber(internationalPhone), new PhoneNumber(fromPhoneNumber),
                    "인증번호 : " + code + " 유효 시간은 5분입니다.").create();
            log.info("Verification code sent successfully to phone: {}", phone);
            return true;
        } catch (Exception e) {
            log.error("Failed to send verification code via phone: {}", e.getMessage(), e);
            return false;
        }
    }

    // 이메일로 인증번호 생성 및 발송 (아이디 찾기 로직 : 이름 + 이메일 조합)
    public boolean sendVerificationCodeByEmailForUserName(String userName, String email) {
        log.info("Attempting to send verification code via email for userName: {} and email: {}", userName, email);

        Optional<UserInfo> user = userRepository.findByUserNameAndUserEmail(userName, email);
        if (user.isEmpty()) {
            log.error("Email verification failed: email not associated with userName: {}", userName);
            return false;  // 이메일이 유효하지 않으면 전송하지 않음
        }

        String code = generateVerificationCode();
        VerificationCode verificationCode = new VerificationCode(code, LocalDateTime.now().plusMinutes(5));
        verificationCodeMap.put(email, verificationCode);  // email로 저장

        try {
            sendEmailVerificationCode(email, code);
            log.info("Verification code sent successfully to email: {}", email);
            return true;
        } catch (Exception e) {
            log.error("Failed to send verification code via email: {}", e.getMessage(), e);
            return false;
        }
    }
    ///////////////////////////////  아이디 찾기 로직 ///////////////////////////////

    //  입력한 전화번호를 국제 전화번호 표기법으로 변환
    private String formatPhoneNumber(String phone) {
        if (phone.startsWith("0")) {
            phone = phone.substring(1);
        }
        return "+82" + phone;
    }

    //  이메일로 인증번호 발송
    private void sendEmailVerificationCode(String email, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Korplace 인증번호");
            message.setText("인증번호는 " + code + " 입니다. 유효시간은 5분입니다. 유효 시간 내에 인증번호를 입력해주세요.");
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("이메일 발송 실패: " + e.getMessage());
        }
    }

    //  인증번호 생성
    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder(VERIFICATION_CODE_LENGTH);
        for (int i = 0; i < VERIFICATION_CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        log.info("인증번호 생성 : {}", code);
        return code.toString();
    }

    //  인증번호 검증
    public boolean verifyCode(String identifier, String code) {
        log.info("Verifying code for identifier: {}", identifier);
        VerificationCode verificationCode = verificationCodeMap.get(identifier);
        if (verificationCode != null && verificationCode.getCode().equals(code)) {
            if (verificationCode.getExpiresAt().isAfter(LocalDateTime.now())) {
                verificationCodeMap.remove(identifier); // 성공 시 삭제
                log.info("Verification code matched and valid for identifier: {}", identifier);
                return true;
            } else {
                verificationCodeMap.remove(identifier); // 만료된 경우 삭제
                log.warn("Verification code expired for identifier: {}", identifier);
            }
        } else {
            log.warn("Invalid verification code for identifier: {}", identifier);
        }
        return false;
    }


    //  임시 비밀번호 발급
    public Optional<String> resetPassword(String userId, String identifier, String option) {

        Optional<UserInfo> user = option.equals("phone") ?
                userRepository.findByUserIdAndUserPhone(userId, identifier) :
                userRepository.findByUserIdAndUserEmail(userId, identifier);

        if (user.isPresent()) {
            String tempPassword = generateTemporaryPassword();
            String hashedPassword = BCrypt.hashpw(tempPassword, BCrypt.gensalt());
            user.get().setUserPassword(hashedPassword);
            userRepository.save(user.get());

            if ("phone".equals(option)) {
                sendSms(identifier, "임시 비밀번호: " + tempPassword);
            } else {
                sendEmailTempPassword(user.get().getUserEmail(), tempPassword);
            }

            return Optional.of(tempPassword);
        }

        return Optional.empty();
    }

    //  임시 비밀번호 생성
    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    //  이메일로 임시 비밀번호 전송
    private void sendEmailTempPassword(String email, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Korplace 임시 비밀번호 발급");
        message.setText("임시 비밀번호는 " + tempPassword + " 입니다.");
        mailSender.send(message);
    }

    //  문자로 임시 비밀번호 전송
    private void sendSms(String phone, String messageText) {
        try {
            String internationalPhone = formatPhoneNumber(phone);
            Message.creator(
                    new PhoneNumber(internationalPhone),
                    new PhoneNumber(fromPhoneNumber),
                    messageText
            ).create();
        } catch (Exception e) {
            throw new RuntimeException("SMS 전송 실패: " + e.getMessage());
        }
    }

    public Optional<String> findUserIdByPhone(String name, String phone) {

        Optional<UserInfo> user = userRepository.findByUserNameAndUserPhone(name, phone);
        if (user.isPresent()) {
            log.info("User found: {}", user.get().getUserId());
            return user.map(UserInfo::getUserId);  // 사용자가 존재하면 아이디 반환
        } else {
            log.warn("No user found with name: {} and Phone: {}", name, phone);
            return Optional.empty();  // 사용자 찾기 실패 시 빈 값 반환
        }
    }

    public Optional<String> findUserIdByEmail(String name, String email) {

        Optional<UserInfo> user = userRepository.findByUserNameAndUserEmail(name, email);
        if (user.isPresent()) {
            log.info("User found: {}", user.get().getUserId());
            return user.map(UserInfo::getUserId);  // 사용자가 존재하면 아이디 반환
        } else {
            log.warn("No user found with name: {} and email: {}", name, email);
            return Optional.empty();  // 사용자 찾기 실패 시 빈 값 반환
        }
    }

    private static class VerificationCode {
        private final String code;
        private final LocalDateTime expiresAt;

        public VerificationCode(String code, LocalDateTime expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
        }

        public String getCode() {
            return code;
        }

        public LocalDateTime getExpiresAt() {
            return expiresAt;
        }
    }

    public UserInfo saveUser(UserInfo userInfo) {
        if (userInfo.getRole() == null || userInfo.getRole().isEmpty()) {
            userInfo.setRole("USER");
        }
        // 비밀번호를 암호화
        String encodedPassword = BCrypt.hashpw(userInfo.getUserPassword(), BCrypt.gensalt());
        userInfo.setUserPassword(encodedPassword);

        // 기본 프로필 이미지가 없으면 기본 이미지 설정
        if (userInfo.getProfileImage() == null || userInfo.getProfileImage().isEmpty()) {
            userInfo.setProfileImage("/uploads/userProfileImg/default-profile-image.png");
        }

        return userRepository.save(userInfo);
    }

    // 비밀번호 변경 로직
    @Transactional
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        try {
        // 사용자 정보 가져오기
        UserInfo user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 현재 비밀번호가 맞는지 확인
        System.out.println("입력된 현재 비밀번호: " + currentPassword);
        System.out.println("저장된 비밀번호 해시: " + user.getUserPassword());


        // 현재 비밀번호가 맞는지 확인
        if (!passwordEncoder.matches(currentPassword, user.getUserPassword())) {
            System.out.println("비밀번호가 일치하지 않음.");
            return false; // 현재 비밀번호가 일치하지 않음
        }

            // 비밀번호 조건 검증
            changeValidatePasswordConditions(newPassword, user.getUserPassword());


            // 새로운 비밀번호가 최근 3개월 내에 사용된 적 있는지 확인
        if (isPasswordUsedRecently(userId, newPassword)) {
            System.out.println("최근 3개월 내에 사용된 비밀번호입니다."); // 비밀번호 재사용 로그
            throw new IllegalArgumentException("최근 3개월 내에 사용한 비밀번호는 재사용할 수 없습니다.");
        }

        System.out.println("비밀번호가 정상적으로 변경되었습니다.");
        // 새 비밀번호 해싱 후 저장
        user.setUserPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 비밀번호 이력 테이블에 새 비밀번호 기록 추가
        PasswordHistory passwordHistory = new PasswordHistory();
        passwordHistory.setUser(user);
        passwordHistory.setPasswordHash(user.getUserPassword());
        passwordHistory.setCreatedAt(LocalDateTime.now());

        // 이력 저장에서 문제가 생기는지 확인
        System.out.println("비밀번호 이력 테이블에 기록되었습니다."); // 이력 저장 로그 추가
        passwordHistoryRepository.save(passwordHistory);

        return true;

    } catch (IllegalArgumentException e) {
        System.out.println("비밀번호 변경 중 예외 발생: " + e.getMessage()); // 예외 발생 로그
        throw e;  // 다시 던져 예외 처리
    } catch (Exception e) {
        System.out.println("비밀번호 변경 중 알 수 없는 오류 발생: " + e.getMessage());
        throw new RuntimeException("비밀번호 변경 중 오류가 발생했습니다.");
    }
    }

    // 최근 3개월 내에 사용된 비밀번호인지 확인하는 메서드
    public boolean isPasswordUsedRecently(Long userId, String newPassword) {
        // 최근 3개월간 사용한 비밀번호 목록 가져오기
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        List<PasswordHistory> recentPasswords = passwordHistoryRepository.findRecentPasswords(userId, threeMonthsAgo);

        // 새로운 비밀번호가 최근 3개월 내에 사용한 비밀번호와 동일한지 확인
        for (PasswordHistory history : recentPasswords) {
            if (passwordEncoder.matches(newPassword, history.getPasswordHash())) {
                return true; // 최근에 사용한 비밀번호임
            }
        }
        return false;
    }

    // 비밀번호 변경 시, 조건 검증
    public void validatePassword(String newPassword) {

        // 최소 8자 이상인지 확인
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.");
        }

        // 대문자와 특수문자를 포함하는지 확인
        if (!newPassword.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("비밀번호는 적어도 하나의 대문자를 포함해야 합니다.");
        }
        if (!newPassword.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            throw new IllegalArgumentException("비밀번호는 적어도 하나의 특수문자를 포함해야 합니다.");
        }
    }

    // 비밀번호 변경 시, 조건 검증
    private void changeValidatePasswordConditions(String newPassword, String currentPasswordHash) {
        // 현재 비밀번호와 동일한지 확인
        if (passwordEncoder.matches(newPassword, currentPasswordHash)) {
            throw new IllegalArgumentException("현재 사용 중인 비밀번호는 재사용할 수 없습니다.");
        }

        // 최소 8자 이상인지 확인
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.");
        }

        // 대문자와 특수문자를 포함하는지 확인
        if (!newPassword.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("비밀번호는 적어도 하나의 대문자를 포함해야 합니다.");
        }
        if (!newPassword.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            throw new IllegalArgumentException("비밀번호는 적어도 하나의 특수문자를 포함해야 합니다.");
        }
    }


    public void updateUserProfile(Long userId, UserInfo updatedInfo) {
        UserInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 기존 값을 덮어쓰지 않도록 null 및 빈 문자열 체크
        if (updatedInfo.getUserName() != null && !updatedInfo.getUserName().isEmpty()) {
            user.setUserName(updatedInfo.getUserName());
        }

        if (updatedInfo.getUserEmail() != null && !updatedInfo.getUserEmail().isEmpty()) {
            user.setUserEmail(updatedInfo.getUserEmail());
        }

        if (updatedInfo.getUserPhone() != null && !updatedInfo.getUserPhone().isEmpty()) {
            user.setUserPhone(updatedInfo.getUserPhone());
        }

        if (updatedInfo.getUserBirthday() != null) {
            user.setUserBirthday(updatedInfo.getUserBirthday());
        }

        // 변경된 내용을 데이터베이스에 저장
        userRepository.save(user);
    }

    //  프로필 이미지 업데이트
    public void updateUserProfileImage(Long userId, String fileName) {
        UserInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.setProfileImage(fileName);

        userRepository.save(user);
    }

    //  프로필 이미지 삭제
    public void deleteUserProfileImage(Long userId) {
        UserInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        String currentProfileImage = user.getProfileImage();

        // 파일 삭제
        if (currentProfileImage != null && !currentProfileImage.isEmpty()) {
            Path imagePath = Paths.get(System.getProperty("user.home"), "Desktop", "uploads", "userProfileImg", currentProfileImage);
            try {
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                throw new RuntimeException("이미지 삭제 실패", e);
            }
        }

        // 프로필 이미지를 기본 이미지로 설정
        user.setProfileImage(null); // 또는 기본 이미지 이름을 설정
        userRepository.save(user);
    }

    public String getUserEmailById(Long userId) {
        return userRepository.findById(userId)
                .map(UserInfo::getUserEmail)  // 유저 이메일 반환
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
    }


    public UserInfo setAdmin(UserInfo userInfo) {
        userInfo.setRole("ADMIN");
        return userRepository.save(userInfo);
    }

    public UserInfo setTestUser(UserInfo userInfo) {
        userInfo.setRole("USER");
        return userRepository.save(userInfo);
    }


    public UserInfo findByUserName(String userName) {
        Optional<UserInfo> userInfo = Optional.ofNullable(userRepository.findByUserName(userName));
        return userInfo.orElse(null);
    }

    public UserInfo findByUserId(String userId) {
        Optional<UserInfo> userInfo = userRepository.findByUserId(userId);
        return userInfo.orElse(null);
    }

    public boolean checkUserIdExists(String userId) {
        return userRepository.existsByUserId(userId);
    }

    public boolean checkUserEmailExists(String userEmail) {
        return userRepository.existsByUserEmail(userEmail);
    }

    public boolean checkUserPhoneExists(String userPhone) {
        return userRepository.existsByUserPhone(userPhone);
    }

    public Optional<UserInfo> getUserById(Long userId) {
        return userRepository.findById(userId);
    }
}
