package side.side.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import side.side.model.GyeonggiEvent;
import side.side.repository.GyeonggiEventRepository;

import java.util.Optional;

@Service
public class GyeonggiEventService {

    @Autowired
    private GyeonggiEventRepository gyeonggiEventRepository;

    // 경기도 댓글 조회
    public Optional<GyeonggiEvent> findById(Long id) {
        return gyeonggiEventRepository.findById(id);
    }
}