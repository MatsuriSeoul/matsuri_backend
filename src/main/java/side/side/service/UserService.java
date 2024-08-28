package side.side.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import side.side.model.UserInfo;
import side.side.repository.UserRepository;

import java.time.LocalDateTime;
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
    private JavaMailSender mailSender;

    private ConcurrentHashMap<String, VerificationCode> verificationCodeMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSid, authToken);
    }

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

    private String formatPhoneNumber(String phone) {
        if (phone.startsWith("0")) {
            phone = phone.substring(1);
        }
        return "+82" + phone;
    }

    private void sendEmailVerificationCode(String email, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Matsuri 인증번호");
            message.setText("인증번호는 " + code + " 입니다. 유효시간은 5분입니다. 유효 시간 내에 인증번호를 입력해주세요.");
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("이메일 발송 실패: " + e.getMessage());
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder(VERIFICATION_CODE_LENGTH);
        for (int i = 0; i < VERIFICATION_CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        log.info("인증번호 생성 : {}", code);
        return code.toString();
    }

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


    public Optional<String> resetPassword(String userId, String identifier, String option) {

        Optional<UserInfo> user = option.equals("phone") ?
                userRepository.findByUserIdAndUserPhone(userId, identifier) :
                userRepository.findByUserIdAndUserEmail(userId, identifier);
// da6cf5c7
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

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private void sendEmailTempPassword(String email, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Matsuri 임시 비밀번호 발급");
        message.setText("임시 비밀번호는 " + tempPassword + " 입니다.");
        mailSender.send(message);
    }

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
        return userRepository.save(userInfo);
    }

    public UserInfo saveAdmin(UserInfo userInfo) {
        userInfo.setRole("ADMIN");
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

    public Optional<UserInfo> getUserById(Long userId) {
        return userRepository.findById(userId);
    }
}
