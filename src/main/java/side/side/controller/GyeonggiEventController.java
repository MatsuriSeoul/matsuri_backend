package side.side.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    // 경기도 행사 시작 월별 및 카테고리별 조회 API
    @GetMapping("/search")
    public ResponseEntity<List<GyeonggiEvent>> getEventsByMonthAndCategory(
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "category", required = true) String category) {

        List<GyeonggiEvent> events;

        if (month == null || month.isEmpty()) {
            // 월이 없는 경우, 즉 "전체"를 선택한 경우 카테고리에 해당하는 모든 데이터를 조회
            events = eventService.getGyeonggiEventsByCategoryMonthNull(category);
        } else {
            // 월이 있는 경우 해당 월과 카테고리에 맞는 데이터 조회
            events = eventService.getGyeonggiEventsByMonthAndCategory(month, category);
        }

        return ResponseEntity.ok(events);
    }

}
