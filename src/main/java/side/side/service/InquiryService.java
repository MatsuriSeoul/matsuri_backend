package side.side.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import side.side.model.Inquiry;
import side.side.model.InquiryResponse;
import side.side.repository.InquiryRepository;
import side.side.repository.InquiryResponseRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InquiryService {

    @Autowired
    private UserService userService;

    @Autowired
    private InquiryRepository inquiryRepository;

    @Autowired
    private InquiryResponseRepository inquiryResponseRepository;

    @Autowired
    private EmailService emailService;  // 메일 알림 발송을 위한 서비스

    public static final String PENDING_RESPONSE = "답변 대기 중";
    public static final String RESPONSE_COMPLETED = "답변 완료";

    @Transactional
    public Inquiry createInquiry(Inquiry inquiry) {
        // 유효성 검사
        if (inquiry.getTitle() == null || inquiry.getTitle().isEmpty()) {
            throw new IllegalArgumentException("문의 제목은 필수입니다.");
        }
        if (inquiry.getContent() == null || inquiry.getContent().isEmpty()) {
            throw new IllegalArgumentException("문의 내용은 필수입니다.");
        }

        inquiry.setCreatedTime(LocalDateTime.now());
        inquiry.setStatus(PENDING_RESPONSE);  // 상수 사용
        return inquiryRepository.save(inquiry);
    }

    public Inquiry updateInquiry(Long inquiryId, Long userId, Inquiry inquiryDetails) {
        Inquiry inquiry = inquiryRepository.findByIdAndUserId(inquiryId, userId)
                .orElseThrow(() -> new RuntimeException("Inquiry not found or access denied"));

        inquiry.setTitle(inquiryDetails.getTitle());
        inquiry.setContent(inquiryDetails.getContent());
        return inquiryRepository.save(inquiry);
    }

    public void deleteInquiry(Long inquiryId, Long userId) {
        Inquiry inquiry = inquiryRepository.findByIdAndUserId(inquiryId, userId)
                .orElseThrow(() -> new RuntimeException("Inquiry not found or access denied"));
        inquiryRepository.delete(inquiry);
    }

    public List<Inquiry> getInquiriesByUserId(Long userId) {
        return inquiryRepository.findByUserId(userId);
    }

    public List<Inquiry> getAllInquiries() {
        return inquiryRepository.findAll();
    }

    public InquiryResponse createResponse(Long inquiryId, InquiryResponse response) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("Inquiry not found with id " + inquiryId));

        response.setInquiry(inquiry);
        response.setRespondedTime(LocalDateTime.now());
        InquiryResponse savedResponse = inquiryResponseRepository.save(response);

        // 답변 상태 변경
        inquiry.setStatus(RESPONSE_COMPLETED);
        inquiryRepository.save(inquiry);

        // 사용자 이메일로 답변 완료 알림 전송
        String userEmail = userService.getUserEmailById(inquiry.getUserId());  // 유저의 이메일을 조회하는 로직
        emailService.sendInquiryResponseNotification(userEmail);

        return savedResponse;
    }
}
