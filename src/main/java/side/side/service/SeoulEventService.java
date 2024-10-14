package side.side.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import side.side.model.SeoulEvent;
import side.side.repository.SeoulEventRepository;

import java.util.List;

@Service
public class SeoulEventService {

    @Autowired
    private SeoulEventRepository seoulEventRepository;

    // svcid로 서울 이벤트 조회
    public SeoulEvent findBySvcid(String svcid) {
        return seoulEventRepository.findBySvcid(svcid);
    }
}