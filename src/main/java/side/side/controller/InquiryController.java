package side.side.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.config.JwtUtils;
import side.side.model.Inquiry;
import side.side.model.Response;
import side.side.service.InquiryService;
import side.side.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/inquiries")
public class InquiryController {

    @Autowired
    private InquiryService inquiryService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<Inquiry> createInquiry(@RequestBody Inquiry inquiry /* , HttpServletRequest request*/) {
//        String token = request.getHeader("Authorization");
//        Long userId = (Long) request.getAttribute("userId");
//        inquiry.setUserId(userId);
        Inquiry createdInquiry = inquiryService.createInquiry(inquiry);
        return ResponseEntity.ok(createdInquiry);
    }

//    @GetMapping
//    public ResponseEntity<List<Inquiry>> getMyInquiries() {
////        Long userId = (Long) request.getAttribute("userId");
//        List<Inquiry> inquiries = inquiryService.getInquiriesByUserId();
//        return ResponseEntity.ok(inquiries);
//    }

    @GetMapping
    public ResponseEntity<List<Inquiry>> getAllInquiry() {
        List<Inquiry> inquiries = inquiryService.getAllInquiries();
        return ResponseEntity.ok(inquiries);
    }

    @PostMapping("/{id}/response")
    public ResponseEntity<Response> createResponse(@PathVariable Long id, @RequestBody Response response) {
        Response createdResponse = inquiryService.createResponse(id, response);
        return ResponseEntity.ok(createdResponse);
    }
}
