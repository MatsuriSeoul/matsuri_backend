package side.side.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.model.*;
import side.side.repository.GyeonggiEventRepository;
import side.side.repository.SeoulEventRepository;
import side.side.repository.TourEventRepository;
import side.side.service.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    @Autowired
    private TourEventService tourEventService;

    @Autowired
    private TourEventRepository tourEventRepository;

    @Autowired
    private GyeonggiEventRepository gyeonggiEventRepository;

    @Autowired
    private SeoulEventRepository seoulEventRepository;

    // 경기 이벤트 데이터 반환 (카테고리 필터링 포함)
    @GetMapping("/gyeonggi-events")
    public List<GyeonggiEvent> getGyeonggiEvents(@RequestParam(required = false) String category) {
        return eventService.getGyeonggiEventsByCategory(category);
    }

    // 경기 이벤트 상세 정보 반환
    @GetMapping("/gyeonggi-events/{id}")
    public ResponseEntity<GyeonggiEvent> getGyeonggiEventById(@PathVariable("id") Long id) {
        Optional<GyeonggiEvent> gyeonggiEvent = gyeonggiEventRepository.findById(id);
        return gyeonggiEvent.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 서울 이벤트 데이터 반환 (카테고리 필터링 포함)
    @GetMapping("/seoul-events")
    public List<SeoulEvent> getSeoulEvents(@RequestParam(required = false) String category) {
        return eventService.getSeoulEventsByCategory(category);
    }

    // 서울 이벤트 상세 정보 반환
    @GetMapping("/seoul-events/{svcid}")
    public ResponseEntity<SeoulEvent> getSeoulEventBySvcId(@PathVariable("svcid") String svcid) {
        SeoulEvent seoulEvent = seoulEventRepository.findBySvcid(svcid);
        if (seoulEvent != null) {
            return ResponseEntity.ok(seoulEvent);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    //국문관광정보 축제/공연/행사 카테고리 데이터
    @GetMapping("/fetch")
    public ResponseEntity<?> fetchEvents(
            @RequestParam String numOfRows,
            @RequestParam String pageNo,
            @RequestParam String eventStartDate) {

        List<TourEvent> events = eventService.fetchAndSaveEvents(numOfRows, pageNo, eventStartDate);
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

        List<FoodEvent> food = foodEventService.fetchAndSaveFoodEvents(numOfRows, pageNo);
        return ResponseEntity.ok(food);
    }

    // 무료 행사 가져오기
    @GetMapping("/free")
    public List<?> getFreeEvents(@RequestParam("region") String region) {
        if ("경기도".equals(region)) {
            return eventService.getGyeonggiFreeEvents();
        } else if ("서울특별시".equals(region)) {
            return eventService.getSeoulFreeEvents();
        }
        return new ArrayList<>();
    }

    // 유료 행사 가져오기
    @GetMapping("/paid")
    public List<?> getPaidEvents(@RequestParam("region") String region) {
        if ("경기도".equals(region)) {
            return eventService.getGyeonggiPaidEvents();
        } else if ("서울특별시".equals(region)) {
            return eventService.getSeoulPaidEvents();
        }
        return new ArrayList<>();
    }
    // 진행 중인 행사 및 예정된 행사 가져오기
    @GetMapping("/scheduled")
    public List<?> getScheduledEvents(@RequestParam("region") String region) {
        if ("경기도".equals(region)) {
            return eventService.getGyeonggiScheduledEvents();
        } else if ("서울특별시".equals(region)) {
            return eventService.getSeoulScheduledEvents();
        }
        return Collections.emptyList();
    }


    /* EventController의 축제/공연/행사에 대한 컨트롤러  */


    // 행사 상세 정보 불러오기
    @GetMapping("/{contentid}/detail")
    public ResponseEntity<?> getEventDetail(@PathVariable String contentid) {
        TourEventDetail detail = eventService.getEventDetailFromDB(contentid);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }

    // 행사 소개 정보 불러오기 (외부 API에서)
    @GetMapping("/{contentid}/{contenttypeid}/intro")
    public ResponseEntity<?> getIntroInfo(@PathVariable String contentid, @PathVariable String contenttypeid) {
        JsonNode introInfo = eventService.fetchIntroInfoFromApi(contentid, contenttypeid);
        if (introInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(introInfo);
    }

    // 이미지 정보 조회 불러오기 (외부 API에서)
    @GetMapping("/{contentid}/images")
    public ResponseEntity<?> getImages(@PathVariable String contentid) {
        JsonNode images = eventService.fetchImagesFromApi(contentid);
        if (images == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(images);
    }

    // 행사 목록 가져오기
    @GetMapping("/fetchAndSaveEvents")
    public ResponseEntity<List<TourEvent>> fetchAndSaveEvents(
            @RequestParam String numOfRows,
            @RequestParam String pageNo,
            @RequestParam String eventStartDate) {

        List<TourEvent> events = eventService.fetchAndSaveEvents(numOfRows, pageNo, eventStartDate);
        return ResponseEntity.ok(events);
    }

    // 키워드 추출
    @GetMapping("/by-region")
    public List<TourEvent> getTourEventsByRegion(@RequestParam String region) {
        return eventService.getTourEventsByRegion(region);
    }

    @GetMapping("/by-region-category")
    public ResponseEntity<List<?>> fetchEventsByRegionAndCategory(
            @RequestParam String region,
            @RequestParam String category) {
        List<?> events = eventService.fetchEventsByCategory(region, category);
        return ResponseEntity.ok(events);
    }
    @GetMapping("/random-by-region")
    public List<TourEvent> getRandomEventsByRegion(@RequestParam("region") String region) {
        return eventService.getRandomEventsByRegion(region);
    }
    // 유사한 여행 코스 정보 가져오기
    @GetMapping("/{contenttypeid}/similar-events")
    public ResponseEntity<List<TourEvent>> getSimilarTourEvent(@PathVariable String contenttypeid) {
        List<TourEvent> similarEvents = eventService.getSimilarTourEvent(contenttypeid);
        if (similarEvents.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(similarEvents);
    }
}

