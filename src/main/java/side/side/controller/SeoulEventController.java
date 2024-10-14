package side.side.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import side.side.model.SeoulEvent;
import side.side.repository.SeoulEventRepository;
import side.side.service.EventService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seoul-events")
public class SeoulEventController {

    @Autowired
    private SeoulEventRepository seoulEventRepository;

    @Autowired
    private EventService eventService;


    // 이미지 URL과 제목만 반환하는 API
    // 이미지를 가지지 않은 경우에는 해당 데이터를 제외하고 반환
    @GetMapping("/titles-and-images")
    public List<String[]> getSeoulEventTitlesAndImages() {
        return seoulEventRepository.findAll().stream()
                .filter(event -> event.getImgurl() != null && !event.getImgurl().isEmpty())
                .map(event -> new String[]{event.getSvcnm(), event.getImgurl()})
                .collect(Collectors.toList());
    }

    // 서울 행사 시작 월별 및 카테고리별 조회 API
    @GetMapping("/search")
    public ResponseEntity<List<SeoulEvent>> getEventsByMonthAndCategory(
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "category", required = true) String category) {

        List<SeoulEvent> events;

        if (month == null || month.isEmpty()) {
            // 월이 없는 경우, 즉 "전체"를 선택한 경우 카테고리에 해당하는 모든 데이터를 조회
            events = eventService.getSeoulEventsByCategoryMonthNull(category);
        } else {
            // 월이 있는 경우 해당 월과 카테고리에 맞는 데이터 조회
            events = eventService.getSeoulEventsByMonthAndCategory(month, category);
        }

        return ResponseEntity.ok(events);
    }

}
