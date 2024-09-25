package side.side.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import side.side.model.GyeonggiEvent;
import side.side.repository.GyeonggiEventRepository;
import side.side.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/api/gyeonggi-events")
public class GyeonggiEventController {

    @Autowired
    private GyeonggiEventRepository gyeonggiEventRepository;

    @Autowired
    private EventService eventService;


    // 경기도 행사 데이터 조회 API
    @GetMapping("/all")
    public List<GyeonggiEvent> getAllGyeonggiEvents() {
        return gyeonggiEventRepository.findAll();
    }

    // 이미지 URL과 제목만 반환하는 API
    @GetMapping("/titles-and-images")
    public List<Object[]> getGyeonggiEventTitlesAndImages() {
        return gyeonggiEventRepository.findTitlesAndImages();
    }
    @GetMapping("/gyeonggi-events")
    public List<GyeonggiEvent> getGyeonggiEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        return eventService.getGyeonggiEventsByCategoryAndDate(category, startDate, endDate);
    }
}
