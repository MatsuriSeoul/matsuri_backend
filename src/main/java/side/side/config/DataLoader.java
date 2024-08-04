package side.side.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import side.side.model.FoodEvent;
import side.side.model.UserInfo;
import side.side.service.*;
import side.side.config.JwtUtils;

import java.util.List;
@Component
public class DataLoader implements ApplicationRunner {

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
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void run(ApplicationArguments args) {
        if (userService.findByUserName("admin") == null) {
            UserInfo admin = new UserInfo();
            admin.setUserId("admin");
            admin.setUserName("admin");
            admin.setUserPassword("1234");
            userService.saveAdmin(admin);
            // 어드민에 대한 토큰 생성
            String token = jwtUtils.generateToken(admin.getUserName(), admin.getId());
            System.out.println("Admin Token: " + token);
        }

        // eventService.fetchAndSaveGyeonggiEvents();
        // eventService.fetchAndSaveSeoulEvents();

//        // 한국관광공사_국문 관광정보 서비스_GW API 자동 호출
//        String serviceKey = "13jkaARutXp/OwAHynRnYjP7BJuMVGIZx2Ki3dRMaDlcBqrfZHC9Zk97LCCuLyKfiR2cVhyWy59t96rPwyWioA==";
//        String numOfRows = "100"; // 한 페이지에 가져올 이벤트 수
//        String pageNo = "1"; // 시작 페이지 번호
//        String eventStartDate = "20240101"; // 이벤트 시작 날짜 (YYYYMMDD 형식)

        // 이벤트 데이터를 저장
        //  eventService.fetchAndSaveEvents(serviceKey, numOfRows, pageNo, eventStartDate);

        // 관광지 데이터 자동 호출
        String numOfRows = "10"; // 한 페이지에 가져올 관광지 수
        String pageNo = "1"; // 시작 페이지 번호

        // 관광지 데이터를 저장
        //touristAttractionsService.fetchAndSaveTouristAttractions(numOfRows, pageNo);

        // 숙박 이벤트 데이터를 저장
        //localEventService.fetchAndSaveEventsLocal(numOfRows, pageNo);

        // 레포츠 데이터를 저장
        //leisureSportsEventService.fetchAndSaveLeisureSportsEvents(numOfRows, pageNo);

        //트레블 코스 데이터 저장
        // travelCourseService.fetchAndSaveTravelCourses(numOfRows,pageNo);

        //문화시설 데이터 저장
        //culturalFacilityService.fetchAndSaveCulturalFacilities(numOfRows, pageNo);

        //쇼핑 데이터 저장
        //shoppingEventService.fetchAndSaveShoppingEvents(numOfRows, pageNo);

        //음식 데이터 저장
        List<FoodEvent> foodEvents = foodEventService.fetchAndSaveFoodEvents(numOfRows, pageNo);
        // 저장된 음식 데이터의 상세 정보를 저장
        for (FoodEvent foodEvent : foodEvents) {
            foodEventService.fetchAndSaveFoodEventDetail(foodEvent.getContentid());
        }
        // 저장된 이벤트의 상세 정보를 업데이트
        updateEventDetails();
    }

    private void updateEventDetails() {
        // 저장된 모든 이벤트의 contentid를 가져와서 상세 정보를 업데이트합니다.
        List<String> contentIds = eventService.getAllContentIds();
        for (String contentId : contentIds) {
            eventService.fetchAndSaveEventDetail(contentId);
        }
    }

}