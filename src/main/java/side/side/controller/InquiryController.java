package side.side.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.config.JwtUtils;
import side.side.model.Inquiry;
import side.side.model.InquiryDTO;
import side.side.model.InquiryResponse;
import side.side.service.InquiryService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/inquiries")
public class InquiryController {

    @Autowired
    private InquiryService inquiryService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<Inquiry> createInquiry(@RequestBody InquiryDTO inquiryDTO, @RequestHeader("Authorization") String token) {
        Long userId = jwtUtils.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        // 로그 추가: DTO에서 받은 공개 여부 값 확인
        System.out.println("전송된 공개 여부 (DTO): " + inquiryDTO.isPublic());



        // DTO에서 받은 값들을 Inquiry로 변환하여 저장
        Inquiry inquiry = new Inquiry();
        inquiry.setTitle(inquiryDTO.getTitle());
        inquiry.setContent(inquiryDTO.getContent());
        inquiry.setPublic(inquiryDTO.isPublic());
        inquiry.setUserId(userId);
        inquiry.setCreatedTime(LocalDateTime.now());
        inquiry.setStatus("답변 대기 중");

        Inquiry createdInquiry = inquiryService.createInquiry(inquiry);
        return ResponseEntity.ok(createdInquiry);
    }

    @GetMapping("/my-inquiries")
    public ResponseEntity<List<Inquiry>> getMyInquiries(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtils.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Inquiry> inquiries = inquiryService.getInquiriesByUserId(userId);
        return ResponseEntity.ok(inquiries);
    }

    @GetMapping
    public ResponseEntity<List<Inquiry>> getAllInquiries() {
        List<Inquiry> inquiries = inquiryService.getAllInquiries();
        return ResponseEntity.ok(inquiries);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Inquiry> updateInquiry(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestBody Inquiry inquiryDetails) {
        Long userId = jwtUtils.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Inquiry updatedInquiry = inquiryService.updateInquiry(id, userId, inquiryDetails);
        return ResponseEntity.ok(updatedInquiry);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInquiry(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        Long userId = jwtUtils.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        inquiryService.deleteInquiry(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/response")
    public ResponseEntity<InquiryResponse> createResponse(@PathVariable Long id, @RequestBody InquiryResponse response) {
        InquiryResponse createdResponse = inquiryService.createResponse(id, response);
        return ResponseEntity.ok(createdResponse);
    }
}
