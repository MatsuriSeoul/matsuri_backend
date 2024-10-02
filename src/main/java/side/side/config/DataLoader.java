package side.side.config;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import side.side.model.*;
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
            admin.setProfileImage("default-profile-image.png");

            // 비밀번호를 암호화 (JBCrypt 사용)
            String hashedPassword = BCrypt.hashpw("1234", BCrypt.gensalt());
            admin.setUserPassword(hashedPassword);

            userService.setAdmin(admin);

            // 어드민에 대한 토큰 생성
            String token = jwtUtils.generateToken(admin.getUserName(), admin.getId(), admin.getRole());
            System.out.println("Admin Token: " + token);
        }

        UserInfo user = new UserInfo();
        user.setUserId("user");
        user.setUserName("TestUser");
        user.setUserEmail("eun4005@gmail.com");
        user.setProfileImage("default-profile-image.png");

        // 비밀번호를 암호화 (JBCrypt 사용)
        String testHashedPassword = BCrypt.hashpw("1234", BCrypt.gensalt());
        user.setUserPassword(testHashedPassword);

        userService.setTestUser(user);

        // 테스트 유저에 대한 토큰 생성
        String token = jwtUtils.generateToken(user.getUserName(), user.getId(), user.getRole());
        System.out.println("Test User Token: " + token);

//         eventService.fetchAndSaveGyeonggiEvents();
//         eventService.fetchAndSaveSeoulEvents();
//
//        // 축제/공연/행사 데이터 호출
//        String eventNumOfRows = "10"; // 한 페이지에 가져올 이벤트 수
//        String eventPageNo = "1"; // 시작 페이지 번호
//        String eventStartDate = "20240101"; // 이벤트 시작 날짜 (YYYYMMDD 형식)
//
//        List<TourEvent> tourEvents = eventService.fetchAndSaveEvents(eventNumOfRows, eventPageNo, eventStartDate);
//        for (TourEvent tourEvent : tourEvents) {
//            eventService.fetchAndSaveEventDetail(tourEvent.getContentid());
//        }
////
////        // 관광지 데이터 호출
//        String touristNumOfRows = "10";
//        String touristPageNo = "1";
//        List<TouristAttraction> touristAttractions = touristAttractionsService.fetchAndSaveTouristAttractions(touristNumOfRows, touristPageNo);
//        for(TouristAttraction touristAttraction : touristAttractions) {
//            touristAttractionsService.fetchAndSaveTouristAttractionDetail(touristAttraction.getContentid());
//        }
////
////        // 숙박 이벤트 데이터 호출
//        String lodgingNumOfRows = "10";
//        String lodgingPageNo = "1";
//        List<LocalEvent> localEvents = localEventService.fetchAndSaveEventsLocal(lodgingNumOfRows, lodgingPageNo);
//        for (LocalEvent localEvent : localEvents) {
//            localEventService.fetchAndSaveLocalEventDetail(localEvent.getContentid());
//        }
////
////        // 레포츠 데이터 호출
//        String leisureNumOfRows = "10";
//        String leisurePageNo = "1";
//        List<LeisureSportsEvent> leisureSportsEvents = leisureSportsEventService.fetchAndSaveLeisureSportsEvents(leisureNumOfRows, leisurePageNo);
//        for (LeisureSportsEvent leisureSportsEvent : leisureSportsEvents) {
//            leisureSportsEventService.fetchAndSaveLeisureSportsEventDetail(leisureSportsEvent.getContentid());
//        }
////
////        // 여행 코스 데이터 호출
//        String travelNumOfRows = "10";
//        String travelPageNo = "1";
//        List<TravelCourse> travelCourses = travelCourseService.fetchAndSaveTravelCourses(travelNumOfRows, travelPageNo);
//        for (TravelCourse travelCourse : travelCourses) {
//            travelCourseService.fetchAndSaveTravelCourseDetail(travelCourse.getContentid());
//        }
//
////        // 문화시설 데이터 호출
//        String culturalNumOfRows = "10";
//        String culturalPageNo = "1";
//        List<CulturalFacility> culturalFacilities = culturalFacilityService.fetchAndSaveCulturalFacilities(culturalNumOfRows, culturalPageNo);
//        for (CulturalFacility culturalFacility : culturalFacilities) {
//            culturalFacilityService.fetchAndSaveCulturalFacilityDetail(culturalFacility.getContentid());
//        }
//
//////        // 쇼핑 데이터 호출
//        String shoppingNumOfRows = "10";
//        String shoppingPageNo = "1";
//        List<ShoppingEvent> shoppingEvents = shoppingEventService.fetchAndSaveShoppingEvents(shoppingNumOfRows, shoppingPageNo);
//        for (ShoppingEvent shoppingEvent : shoppingEvents) {
//            shoppingEventService.fetchAndSaveShoppingEventDetail(shoppingEvent.getContentid());
//        }
//////
//////        // 음식 데이터 호출
//        String foodNumOfRows = "10";
//        String foodPageNo = "1";
//        List<FoodEvent> foodEvents = foodEventService.fetchAndSaveFoodEvents(foodNumOfRows, foodPageNo);
//        for (FoodEvent foodEvent : foodEvents) {
//            foodEventService.fetchAndSaveFoodEventDetail(foodEvent.getContentid());
//        }
//
//        // 저장된 이벤트의 상세 정보를 업데이트
//        updateEventDetails();
//    }
////
//    private void updateEventDetails() {
//        // 저장된 모든 이벤트의 contentid를 가져와서 상세 정보를 업데이트
//        List<String> contentIds = eventService.getAllContentIds();
//        for (String contentId : contentIds) {
//            eventService.fetchAndSaveEventDetail(contentId);
//        }
    }
}
