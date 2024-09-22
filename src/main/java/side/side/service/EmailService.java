package side.side.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);  // 수신자 이메일 주소
        message.setSubject(subject);  // 메일 제목
        message.setText(body);  // 메일 내용
        message.setFrom("korplace2024@gmail.com");  // 발신자 이메일 주소

        mailSender.send(message);  // 이메일 전송
    }

    public void sendInquiryResponseNotification(String userEmail) {
        // 답변 완료 알림 이메일 발송
        String subject = "문의에 대한 답변이 완료되었습니다.";
        String body = "문의하신 내용에 대한 답변이 완료되었습니다. 상세 답변은 웹사이트에서 확인해 주세요.";
        sendEmail(userEmail, subject, body);
    }
}
