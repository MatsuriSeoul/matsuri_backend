package side.side.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import side.side.model.Inquiry;
import side.side.model.Response;
import side.side.repository.InquiryRepository;
import side.side.repository.ResponseRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InquiryService {

    @Autowired
    private InquiryRepository inquiryRepository;

    @Autowired
    private ResponseRepository responseRepository;

    public Inquiry createInquiry(Inquiry inquiry) {
        inquiry.setCreatedTime(LocalDateTime.now());
        return inquiryRepository.save(inquiry);
    }

//    public List<Inquiry> getInquiriesByUserId(Long userId) {
//        return inquiryRepository.findByUserId(userId);
//    }

    public List<Inquiry> getAllInquiries() {
        return inquiryRepository.findAll();
    }

    public Response createResponse(Long inquiryId, Response response) {
        Optional<Inquiry> inquiryOptional = inquiryRepository.findById(inquiryId);
        if (inquiryOptional.isPresent()) {
            Inquiry inquiry = inquiryOptional.get();
            response.setInquiry(inquiry);
            response.setRespondedTime(LocalDateTime.now());
            return responseRepository.save(response);
        } else {
            throw new RuntimeException("Inquiry not found with id " + inquiryId);
        }
    }
}


