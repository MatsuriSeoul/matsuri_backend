package side.side.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import side.side.model.TourEvent;
import side.side.model.TourEventDetail;
import side.side.service.EventService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @GetMapping("/fetchGyeonggi")
    public String fetchGyeonggiEvents() {
        eventService.fetchAndSaveGyeonggiEvents();
        return "경기도 api 저장 완료";
    }

    @GetMapping("/fetchSeoul")
    public String fetchSeoulEvents() {
        eventService.fetchAndSaveSeoulEvents();
        return "서울 api 저장 완료";
    }

    @GetMapping("/search")
    public List<Object> searchEvents(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String category
    ) {
        return eventService.searchEvents(date, region, category);
    }
    //국문관광정보 행사 데이터
    @GetMapping("/fetch")
    public ResponseEntity<?> fetchEvents(
            @RequestParam String serviceKey,
            @RequestParam String numOfRows,
            @RequestParam String pageNo,
            @RequestParam String eventStartDate) {

        List<TourEvent> events = eventService.fetchAndSaveEvents(serviceKey, numOfRows, pageNo, eventStartDate);
        return ResponseEntity.ok(events);
    }
    //국문관광정보 지역 데이터
    @GetMapping("/fetchAndSave")
    public ResponseEntity<List<TourEvent>> fetchAndSaveEventsLocal(
            @RequestParam String numOfRows,
            @RequestParam String pageNo) {
        List<TourEvent> events = eventService.fetchAndSaveEventsLocal(numOfRows, pageNo);
        return ResponseEntity.ok(events);
    }

}
