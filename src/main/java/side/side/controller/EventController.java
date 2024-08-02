package side.side.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import side.side.model.LocalEvent;
import side.side.model.TourEvent;
import side.side.model.TourEventDetail;
import side.side.model.TouristAttraction;
import side.side.service.EventService;
import side.side.service.LeisureSportsEventService;
import side.side.service.LocalEventService;
import side.side.service.TouristAttractionsService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private LocalEventService localEventService;

    @Autowired
    private TouristAttractionsService touristAttractionsService;


    @Autowired
    private LeisureSportsEventService leisureSportsEventService;

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
    public ResponseEntity<List<LocalEvent>> fetchAndSaveEventsLocal(
            @RequestParam String numOfRows,
            @RequestParam String pageNo) {
        List<LocalEvent> events = localEventService.fetchAndSaveEventsLocal(numOfRows, pageNo);
        return ResponseEntity.ok(events);
    }
    // 관광지 데이터 불러오기
    @GetMapping("/fetchAndSaveTouristAttractions")
    public ResponseEntity<List<TouristAttraction>> fetchAndSaveTouristAttractions(
            @RequestParam String numOfRows,
            @RequestParam String pageNo) {

        List<TouristAttraction> attractions = touristAttractionsService.fetchAndSaveTouristAttractions(numOfRows, pageNo);
        return ResponseEntity.ok(attractions);
    }

    // 레포츠 데이터 불러오기
    @GetMapping("/fetchAndSaveLeisureSports")
    public ResponseEntity<List<?>> fetchAndSaveLeisureSports(
            @RequestParam String numOfRows,
            @RequestParam String pageNo) {
        List<?> events = leisureSportsEventService.fetchAndSaveLeisureSportsEvents(numOfRows, pageNo);
        return ResponseEntity.ok(events);
    }
}
