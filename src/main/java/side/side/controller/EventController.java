package side.side.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import side.side.model.*;
import side.side.service.*;

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

    @Autowired
    private TravelCourseService travelCourseService;

    @Autowired
    private CulturalFacilityService culturalFacilityService;

    @Autowired
    private ShoppingEventService shoppingEventService;

    @Autowired
    private FoodEventService foodEventService;

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

    //국문관광정보 축제/공연/행사 카테고리 데이터
    @GetMapping("/fetch")
    public ResponseEntity<?> fetchEvents(
            @RequestParam String serviceKey,
            @RequestParam String numOfRows,
            @RequestParam String pageNo,
            @RequestParam String eventStartDate) {

        List<TourEvent> events = eventService.fetchAndSaveEvents(serviceKey, numOfRows, pageNo, eventStartDate);
        return ResponseEntity.ok(events);
    }

    //국문관광정보 지역(숙박) 카테고리 데이터
    @GetMapping("/fetchAndSave")
    public ResponseEntity<List<LocalEvent>> fetchAndSaveEventsLocal(
            @RequestParam String numOfRows,
            @RequestParam String pageNo) {
        List<LocalEvent> events = localEventService.fetchAndSaveEventsLocal(numOfRows, pageNo);
        return ResponseEntity.ok(events);
    }

    // 관광지 카테고리 데이터 불러오기
    @GetMapping("/fetchAndSaveTouristAttractions")
    public ResponseEntity<List<TouristAttraction>> fetchAndSaveTouristAttractions(
            @RequestParam String numOfRows,
            @RequestParam String pageNo) {

        List<TouristAttraction> attractions = touristAttractionsService.fetchAndSaveTouristAttractions(numOfRows, pageNo);
        return ResponseEntity.ok(attractions);
    }

    // 레포츠 카테고리 데이터 불러오기
    @GetMapping("/fetchAndSaveLeisureSports")
    public ResponseEntity<List<?>> fetchAndSaveLeisureSports(
            @RequestParam String numOfRows,
            @RequestParam String pageNo) {
        List<?> events = leisureSportsEventService.fetchAndSaveLeisureSportsEvents(numOfRows, pageNo);
        return ResponseEntity.ok(events);
    }

    // 여행 코스 카테고리 데이터 불러오기
    @GetMapping("/fetchAndSaveTravel")
    public ResponseEntity<List<TravelCourse>> fetchAndSaveTravelCourses(
            @RequestParam String numOfRows,
            @RequestParam String pageNo) {

        List<TravelCourse> courses = travelCourseService.fetchAndSaveTravelCourses(numOfRows, pageNo);
        return ResponseEntity.ok(courses);
    }

    //문화 시설 카테고리 데이터 불러오기
    @GetMapping("/fetchAndSaveCulturalFacility")
    public ResponseEntity<List<CulturalFacility>> fetchAndSaveCulturalFacilities(
            @RequestParam String numOfRows,
            @RequestParam String pageNo) {

        List<CulturalFacility> facilities = culturalFacilityService.fetchAndSaveCulturalFacilities(numOfRows, pageNo);
        return ResponseEntity.ok(facilities);
    }

    //쇼핑 카테고리 데이터 불러오기
    @GetMapping("/fetchAndSaveShopping")
    public ResponseEntity<List<ShoppingEvent>> fetchAndSaveShoppingEvents(
            @RequestParam String numOfRows,
            @RequestParam String pageNo) {

        List<ShoppingEvent> shopping = shoppingEventService.fetchAndSaveShoppingEvents(numOfRows, pageNo);
        return ResponseEntity.ok(shopping);
    }
    // 음식 카테고리 데이터 불러오기
    @GetMapping("/fetchAndSaveFood")
    public ResponseEntity<List<FoodEvent>> fetchAndSaveFoodEvents(
            @RequestParam String numOfRows,
            @RequestParam String pageNo) {

        List<FoodEvent> food = foodEventService.fetchAndSaveFoodEvents(numOfRows,pageNo);
        return ResponseEntity.ok(food);
    }
}
