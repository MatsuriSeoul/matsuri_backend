package side.side.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
    @GetMapping("/seoul-events")
    public List<SeoulEvent> getSeoulEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        return eventService.getSeoulEventsByCategoryAndDate(category, startDate, endDate);
    }
}
