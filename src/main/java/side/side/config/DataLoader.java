package side.side.config;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import side.side.model.*;
import side.side.repository.LocalBasedRepository;
import side.side.service.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Autowired
    private LocalBasedService localBasedService;
    @Autowired
    private LocalBasedRepository localBasedRepository;

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
        // 서버 시작 시 지역별 관광정보를 가져와 저장
 //       fetchAllLocalEvents();

//        // 축제/공연/행사 데이터 호출
//        String eventNumOfRows = "10"; // 한 페이지에 가져올 이벤트 수
//        String eventPageNo = "1"; // 시작 페이지 번호
//        String eventStartDate = "20240101"; // 이벤트 시작 날짜 (YYYYMMDD 형식)
//
//        List<TourEvent> tourEvents = eventService.fetchAndSaveEvents(eventNumOfRows, eventPageNo, eventStartDate);
//        for (TourEvent tourEvent : tourEvents) {
//            eventService.fetchAndSaveEventDetail(tourEvent.getContentid());
//        }
//
//        // 관광지 데이터 호출
//        String touristNumOfRows = "10";
//        String touristPageNo = "1";
//        List<TouristAttraction> touristAttractions = touristAttractionsService.fetchAndSaveTouristAttractions(touristNumOfRows, touristPageNo);
//        for(TouristAttraction touristAttraction : touristAttractions) {
//            touristAttractionsService.fetchAndSaveTouristAttractionDetail(touristAttraction.getContentid());
//        }
//
//        // 숙박 이벤트 데이터 호출
//        String lodgingNumOfRows = "10";
//        String lodgingPageNo = "1";
//        List<LocalEvent> localEvents = localEventService.fetchAndSaveEventsLocal(lodgingNumOfRows, lodgingPageNo);
//        for (LocalEvent localEvent : localEvents) {
//            localEventService.fetchAndSaveLocalEventDetail(localEvent.getContentid());
//        }
//
//        // 레포츠 데이터 호출
//        String leisureNumOfRows = "10";
//        String leisurePageNo = "1";
//        List<LeisureSportsEvent> leisureSportsEvents = leisureSportsEventService.fetchAndSaveLeisureSportsEvents(leisureNumOfRows, leisurePageNo);
//        for (LeisureSportsEvent leisureSportsEvent : leisureSportsEvents) {
//            leisureSportsEventService.fetchAndSaveLeisureSportsEventDetail(leisureSportsEvent.getContentid());
//       }
//
//        // 여행 코스 데이터 호출
//        String travelNumOfRows = "10";
//        String travelPageNo = "1";
//        List<TravelCourse> travelCourses = travelCourseService.fetchAndSaveTravelCourses(travelNumOfRows, travelPageNo);
//        for (TravelCourse travelCourse : travelCourses) {
//            travelCourseService.fetchAndSaveTravelCourseDetail(travelCourse.getContentid());
//        }
//
//        // 문화시설 데이터 호출
//        String culturalNumOfRows = "10";
//        String culturalPageNo = "1";
//        List<CulturalFacility> culturalFacilities = culturalFacilityService.fetchAndSaveCulturalFacilities(culturalNumOfRows, culturalPageNo);
//        for (CulturalFacility culturalFacility : culturalFacilities) {
//            culturalFacilityService.fetchAndSaveCulturalFacilityDetail(culturalFacility.getContentid());
//        }
//
//        // 쇼핑 데이터 호출
//        String shoppingNumOfRows = "10";
//        String shoppingPageNo = "1";
//        List<ShoppingEvent> shoppingEvents = shoppingEventService.fetchAndSaveShoppingEvents(shoppingNumOfRows, shoppingPageNo);
//        for (ShoppingEvent shoppingEvent : shoppingEvents) {
//            shoppingEventService.fetchAndSaveShoppingEventDetail(shoppingEvent.getContentid());
//        }
//
//        // 음식 데이터 호출
//        String foodNumOfRows = "10";
//        String foodPageNo = "1";
//        List<FoodEvent> foodEvents = foodEventService.fetchAndSaveFoodEvents(foodNumOfRows, foodPageNo);
//        for (FoodEvent foodEvent : foodEvents) {
//            foodEventService.fetchAndSaveFoodEventDetail(foodEvent.getContentid());
//        }

        // 저장된 이벤트의 상세 정보를 업데이트
        updateEventDetails();
    }
    // LocalBasedService를 이용하여 도시 지역(예시)의 관광정보를 서버 시작 시 가져와 저장
//    public void fetchAllLocalEvents() {
//        int numOfRows = EventFetchConfig.DEFAULT_NUM_OF_ROWS;
//
//        // 지역별로 관광 정보를 가져와 저장
//        fetchAndSaveLocalGyeonggiEvents(numOfRows);
//        fetchAndSaveLocalSeoulEvents(numOfRows);
//        fetchAndSaveLocalGangwonEvents(numOfRows);
//        fetchAndSaveLocalInchoenEvents(numOfRows);
//        fetchAndSaveLocalChungbukEvents(numOfRows);
//        fetchAndSaveLocalChungnamEvents(numOfRows);
//        fetchAndSaveLocalDaejeonEvents(numOfRows);
//        fetchAndSaveLocalDaeguEvents(numOfRows);
//        fetchAndSaveLocalGyeongbukEvents(numOfRows);
//        fetchAndSaveLocalGyeongnamEvents(numOfRows);
//        fetchAndSaveLocalGwangjuEvents(numOfRows);
//        fetchAndSaveLocalJeonnamEvents(numOfRows);
//        fetchAndSaveLocalJeonbukEvents(numOfRows);
//        fetchAndSaveLocalUlsanEvents(numOfRows);
//        fetchAndSaveLocalSejongEvents(numOfRows);
//        fetchAndSaveLocalJejuEvents(numOfRows);
//        fetchAndSaveLocalBusanEvents(numOfRows);
//
//    }



    //
    private void updateEventDetails() {
        // 저장된 모든 이벤트의 contentid를 가져와서 상세 정보를 업데이트
        List<String> contentIds = eventService.getAllContentIds();
        for (String contentId : contentIds) {
            eventService.fetchAndSaveEventDetail(contentId);
        }
    }
    public class EventFetchConfig {
        public static final int DEFAULT_NUM_OF_ROWS = 10; // 기본 최대 데이터 수
    }


    // LocalBasedService를 이용하여 도시 지역(예시)의 관광정보를 서버 시작 시 가져와 저장
    private void fetchAndSaveLocalGyeonggiEvents(int numOfRows) {
        try {
            String region = "gyeonggi"; // 경기도 지역을 지정
            String pageNo = "1"; // 첫 페이지부터 시작

            // 경기도의 시군구 코드 목록
            int[] sigunguCodes = {
                    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                    21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31
            };

            int totalEvents = 0;

            // 각 시군구에 대해 데이터를 가져오는 반복문
            for (int sigunguCode : sigunguCodes) {
                List<LocalBase> events = localBasedService.fetchAndSaveEvents(region, sigunguCode, numOfRows, pageNo);

                // 중복 데이터 필터링 및 저장
                List<LocalBase> filteredEvents = new ArrayList<>();
                for (LocalBase event : events) {
                    // 데이터베이스에 존재하지 않는 경우만 저장
                    if (!localBasedRepository.existsByContentid(event.getContentid())) {
                        filteredEvents.add(event);
                    }
                }

                if (!filteredEvents.isEmpty()) {
                    localBasedRepository.saveAll(filteredEvents); // 중복 필터링된 데이터만 저장
                    System.out.println("데이터 저장 : " + filteredEvents.size() + "지역의 데이터 : " + region + ", 시/군/구 : " + sigunguCode);
                    totalEvents += filteredEvents.size();
                } else {
                    System.out.println("지역의 새로운 데이터를 받아오지 못함 : : " + region + ", 시/군/구 : " + sigunguCode);
                }
            }

            System.out.println("경기 지역에서 가져오고 저장된 총 이벤트 :" + totalEvents);

        } catch (Exception e) {
            System.err.println("경기 지역에 대한 이벤트를 가져오고 저장하는 중 오류가 발생했습니다. : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fetchAndSaveLocalSeoulEvents(int numOfRows) {
        try {
            String region = "seoul";
            String pageNo = "1";     // 첫 페이지부터 시작

            // 서울특별시의 시군구 코드 목록
            int[] sigunguCodes = {
                    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                    21, 22, 23, 24, 25
            };

            int totalEvents = 0;

            // 각 시군구에 대해 데이터를 가져오는 반복문
            for (int sigunguCode : sigunguCodes) {
                List<LocalBase> events = localBasedService.fetchAndSaveEvents(region, sigunguCode, Integer.parseInt(String.valueOf(numOfRows)), pageNo);

                // 중복 데이터 필터링 및 저장
                List<LocalBase> filteredEvents = new ArrayList<>();
                for (LocalBase event : events) {
                    // 데이터베이스에 존재하지 않는 경우만 저장
                    if (!localBasedRepository.existsByContentid(event.getContentid())) {
                        filteredEvents.add(event);
                    }
                }

                if (!filteredEvents.isEmpty()) {
                    localBasedRepository.saveAll(filteredEvents); // 중복 필터링된 데이터만 저장
                    System.out.println("데이터 저장 : " + filteredEvents.size() + "지역의 데이터 : " + region + ", 시/군/구 : " + sigunguCode);
                    totalEvents += filteredEvents.size();
                } else {
                    System.out.println("지역의 새로운 데이터를 받아오지 못함 : : " + region + ", 시/군/구 : " + sigunguCode);
                }
            }

            System.out.println("경기 지역에서 가져오고 저장된 총 이벤트 :" + totalEvents);

        } catch (Exception e) {
            System.err.println("경기 지역에 대한 이벤트를 가져오고 저장하는 중 오류가 발생했습니다. : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fetchAndSaveLocalInchoenEvents(int numOfRows) {
        try {
            String region = "incheon";
            String pageNo = "1";     // 첫 페이지부터 시작

            // 인천광역시의 시군구 코드 목록
            int[] sigunguCodes = {
                    1, 2, 3, 4, 5, 6, 7, 8, 9, 10
            };

            int totalEvents = 0;

            // 각 시군구에 대해 데이터를 가져오는 반복문
            for (int sigunguCode : sigunguCodes) {
                List<LocalBase> events = localBasedService.fetchAndSaveEvents(region, sigunguCode, Integer.parseInt(String.valueOf(numOfRows)), pageNo);

                // 중복 데이터 필터링 및 저장
                List<LocalBase> filteredEvents = new ArrayList<>();
                for (LocalBase event : events) {
                    // 데이터베이스에 존재하지 않는 경우만 저장
                    if (!localBasedRepository.existsByContentid(event.getContentid())) {
                        filteredEvents.add(event);
                    }
                }

                if (!filteredEvents.isEmpty()) {
                    localBasedRepository.saveAll(filteredEvents); // 중복 필터링된 데이터만 저장
                    System.out.println("데이터 저장 : " + filteredEvents.size() + "지역의 데이터 : " + region + ", 시/군/구 : " + sigunguCode);
                    totalEvents += filteredEvents.size();
                } else {
                    System.out.println("지역의 새로운 데이터를 받아오지 못함 : : " + region + ", 시/군/구 : " + sigunguCode);
                }
            }

            System.out.println("경기 지역에서 가져오고 저장된 총 이벤트 :" + totalEvents);

        } catch (Exception e) {
            System.err.println("경기 지역에 대한 이벤트를 가져오고 저장하는 중 오류가 발생했습니다. : " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void fetchAndSaveLocalChungbukEvents(int numOfRows) {
        try {
            String region = "chungbuk";
            String pageNo = "1";     // 첫 페이지부터 시작

            // 충청북도의 시군구 코드 목록
            int[] sigunguCodes = {
                    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12
            };

            int totalEvents = 0;

            // 각 시군구에 대해 데이터를 가져오는 반복문
            for (int sigunguCode : sigunguCodes) {
                List<LocalBase> events = localBasedService.fetchAndSaveEvents(region, sigunguCode, Integer.parseInt(String.valueOf(numOfRows)), pageNo);

                // 중복 데이터 필터링 및 저장
                List<LocalBase> filteredEvents = new ArrayList<>();
                for (LocalBase event : events) {
                    // 데이터베이스에 존재하지 않는 경우만 저장
                    if (!localBasedRepository.existsByContentid(event.getContentid())) {
                        filteredEvents.add(event);
                    }
                }

                if (!filteredEvents.isEmpty()) {
                    localBasedRepository.saveAll(filteredEvents); // 중복 필터링된 데이터만 저장
                    System.out.println("데이터 저장 : " + filteredEvents.size() + "지역의 데이터 : " + region + ", 시/군/구 : " + sigunguCode);
                    totalEvents += filteredEvents.size();
                } else {
                    System.out.println("지역의 새로운 데이터를 받아오지 못함 : : " + region + ", 시/군/구 : " + sigunguCode);
                }
            }

            System.out.println("경기 지역에서 가져오고 저장된 총 이벤트 :" + totalEvents);

        } catch (Exception e) {
            System.err.println("경기 지역에 대한 이벤트를 가져오고 저장하는 중 오류가 발생했습니다. : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fetchAndSaveLocalChungnamEvents(int numOfRows) {
        try {
            String region = "chungnam";
            String pageNo = "1";     // 첫 페이지부터 시작

            // 충청남도의 시군구 코드 목록
            int[] sigunguCodes = {
                    1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13, 14, 15, 16
            };

            int totalEvents = 0;

            // 각 시군구에 대해 데이터를 가져오는 반복문
            for (int sigunguCode : sigunguCodes) {
                List<LocalBase> events = localBasedService.fetchAndSaveEvents(region, sigunguCode, Integer.parseInt(String.valueOf(numOfRows)), pageNo);

                // 중복 데이터 필터링 및 저장
                List<LocalBase> filteredEvents = new ArrayList<>();
                for (LocalBase event : events) {
                    // 데이터베이스에 존재하지 않는 경우만 저장
                    if (!localBasedRepository.existsByContentid(event.getContentid())) {
                        filteredEvents.add(event);
                    }
                }

                if (!filteredEvents.isEmpty()) {
                    localBasedRepository.saveAll(filteredEvents); // 중복 필터링된 데이터만 저장
                    System.out.println("데이터 저장 : " + filteredEvents.size() + "지역의 데이터 : " + region + ", 시/군/구 : " + sigunguCode);
                    totalEvents += filteredEvents.size();
                } else {
                    System.out.println("지역의 새로운 데이터를 받아오지 못함 : : " + region + ", 시/군/구 : " + sigunguCode);
                }
            }

            System.out.println("경기 지역에서 가져오고 저장된 총 이벤트 :" + totalEvents);

        } catch (Exception e) {
            System.err.println("경기 지역에 대한 이벤트를 가져오고 저장하는 중 오류가 발생했습니다. : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fetchAndSaveLocalDaejeonEvents(int numOfRows) {
        try {
            String region = "daejeon";
            String pageNo = "1";     // 첫 페이지부터 시작

            // 대전광역시의 시군구 코드 목록
            int[] sigunguCodes = {
                    1, 2, 3, 4, 5
            };

            int totalEvents = 0;

            // 각 시군구에 대해 데이터를 가져오는 반복문
            for (int sigunguCode : sigunguCodes) {
                List<LocalBase> events = localBasedService.fetchAndSaveEvents(region, sigunguCode, Integer.parseInt(String.valueOf(numOfRows)), pageNo);

                // 중복 데이터 필터링 및 저장
                List<LocalBase> filteredEvents = new ArrayList<>();
                for (LocalBase event : events) {
                    // 데이터베이스에 존재하지 않는 경우만 저장
                    if (!localBasedRepository.existsByContentid(event.getContentid())) {
                        filteredEvents.add(event);
                    }
                }

                if (!filteredEvents.isEmpty()) {
                    localBasedRepository.saveAll(filteredEvents); // 중복 필터링된 데이터만 저장
                    System.out.println("데이터 저장 : " + filteredEvents.size() + "지역의 데이터 : " + region + ", 시/군/구 : " + sigunguCode);
                    totalEvents += filteredEvents.size();
                } else {
                    System.out.println("지역의 새로운 데이터를 받아오지 못함 : : " + region + ", 시/군/구 : " + sigunguCode);
                }
            }

            System.out.println("경기 지역에서 가져오고 저장된 총 이벤트 :" + totalEvents);

        } catch (Exception e) {
            System.err.println("경기 지역에 대한 이벤트를 가져오고 저장하는 중 오류가 발생했습니다. : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fetchAndSaveLocalDaeguEvents(int numOfRows) {
        try {
            String region = "daegu";
            String pageNo = "1";     // 첫 페이지부터 시작

            // 대구광역시의 시군구 코드 목록
            int[] sigunguCodes = {
                    1, 2, 3, 4, 5, 6, 7, 8, 9
            };

            int totalEvents = 0;

            // 각 시군구에 대해 데이터를 가져오는 반복문
            for (int sigunguCode : sigunguCodes) {
                List<LocalBase> events = localBasedService.fetchAndSaveEvents(region, sigunguCode, Integer.parseInt(String.valueOf(numOfRows)), pageNo);

                // 중복 데이터 필터링 및 저장
                List<LocalBase> filteredEvents = new ArrayList<>();
                for (LocalBase event : events) {
                    // 데이터베이스에 존재하지 않는 경우만 저장
                    if (!localBasedRepository.existsByContentid(event.getContentid())) {
                        filteredEvents.add(event);
                    }
                }

                if (!filteredEvents.isEmpty()) {
                    localBasedRepository.saveAll(filteredEvents); // 중복 필터링된 데이터만 저장
                    System.out.println("데이터 저장 : " + filteredEvents.size() + "지역의 데이터 : " + region + ", 시/군/구 : " + sigunguCode);
                    totalEvents += filteredEvents.size();
                } else {
                    System.out.println("지역의 새로운 데이터를 받아오지 못함 : : " + region + ", 시/군/구 : " + sigunguCode);
                }
            }

            System.out.println("경기 지역에서 가져오고 저장된 총 이벤트 :" + totalEvents);

        } catch (Exception e) {
            System.err.println("경기 지역에 대한 이벤트를 가져오고 저장하는 중 오류가 발생했습니다. : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fetchAndSaveLocalGyeongbukEvents(int numOfRows) {
        try {
            String region = "gyeongbuk";
            String pageNo = "1";     // 첫 페이지부터 시작

            // 경상북도의 시군구 코드 목록
            int[] sigunguCodes = {
                    1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23
            };

            int totalEvents = 0;

            // 각 시군구에 대해 데이터를 가져오는 반복문
            for (int sigunguCode : sigunguCodes) {
                List<LocalBase> events = localBasedService.fetchAndSaveEvents(region, sigunguCode, Integer.parseInt(String.valueOf(numOfRows)), pageNo);

                // 중복 데이터 필터링 및 저장
                List<LocalBase> filteredEvents = new ArrayList<>();
                for (LocalBase event : events) {
                    // 데이터베이스에 존재하지 않는 경우만 저장
                    if (!localBasedRepository.existsByContentid(event.getContentid())) {
                        filteredEvents.add(event);
                    }
                }

                if (!filteredEvents.isEmpty()) {
                    localBasedRepository.saveAll(filteredEvents); // 중복 필터링된 데이터만 저장
                    System.out.println("데이터 저장 : " + filteredEvents.size() + "지역의 데이터 : " + region + ", 시/군/구 : " + sigunguCode);
                    totalEvents += filteredEvents.size();
                } else {
                    System.out.println("지역의 새로운 데이터를 받아오지 못함 : : " + region + ", 시/군/구 : " + sigunguCode);
                }
            }

            System.out.println("경기 지역에서 가져오고 저장된 총 이벤트 :" + totalEvents);

        } catch (Exception e) {
            System.err.println("경기 지역에 대한 이벤트를 가져오고 저장하는 중 오류가 발생했습니다. : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fetchAndSaveLocalGyeongnamEvents(int numOfRows) {
        try {
            String region = "gyeongnam";
            String pageNo = "1";     // 첫 페이지부터 시작

            // 경상남도의 시군구 코드 목록
            int[] sigunguCodes = {
                    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21
            };

            int totalEvents = 0;

            // 각 시군구에 대해 데이터를 가져오는 반복문
            for (int sigunguCode : sigunguCodes) {
                List<LocalBase> events = localBasedService.fetchAndSaveEvents(region, sigunguCode, Integer.parseInt(String.valueOf(numOfRows)), pageNo);

                // 중복 데이터 필터링 및 저장
                List<LocalBase> filteredEvents = new ArrayList<>();
                for (LocalBase event : events) {
                    // 데이터베이스에 존재하지 않는 경우만 저장
                    if (!localBasedRepository.existsByContentid(event.getContentid())) {
                        filteredEvents.add(event);
                    }
                }

                if (!filteredEvents.isEmpty()) {
                    localBasedRepository.saveAll(filteredEvents); // 중복 필터링된 데이터만 저장
                    System.out.println("데이터 저장 : " + filteredEvents.size() + "지역의 데이터 : " + region + ", 시/군/구 : " + sigunguCode);
                    totalEvents += filteredEvents.size();
                } else {
                    System.out.println("지역의 새로운 데이터를 받아오지 못함 : : " + region + ", 시/군/구 : " + sigunguCode);
                }
            }

            System.out.println("경기 지역에서 가져오고 저장된 총 이벤트 :" + totalEvents);

        } catch (Exception e) {
            System.err.println("경기 지역에 대한 이벤트를 가져오고 저장하는 중 오류가 발생했습니다. : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fetchAndSaveLocalGwangjuEvents(int numOfRows) {
        try {
            String region = "gwangju";
            String pageNo = "1";     // 첫 페이지부터 시작

            // 광주광역시의 시군구 코드 목록
            int[] sigunguCodes = {
                    1, 2, 3, 4, 5
            };
            int totalEvents = 0;

            // 각 시군구에 대해 데이터를 가져오는 반복문
            for (int sigunguCode : sigunguCodes) {
                List<LocalBase> events = localBasedService.fetchAndSaveEvents(region, sigunguCode, Integer.parseInt(String.valueOf(numOfRows)), pageNo);

                // 중복 데이터 필터링 및 저장
                List<LocalBase> filteredEvents = new ArrayList<>();
                for (LocalBase event : events) {
                    // 데이터베이스에 존재하지 않는 경우만 저장
                    if (!localBasedRepository.existsByContentid(event.getContentid())) {
                        filteredEvents.add(event);
                    }
                }

                if (!filteredEvents.isEmpty()) {
                    localBasedRepository.saveAll(filteredEvents); // 중복 필터링된 데이터만 저장
                    System.out.println("데이터 저장 : " + filteredEvents.size() + "지역의 데이터 : " + region + ", 시/군/구 : " + sigunguCode);
                    totalEvents += filteredEvents.size();
                } else {
                    System.out.println("지역의 새로운 데이터를 받아오지 못함 : : " + region + ", 시/군/구 : " + sigunguCode);
                }
            }

            System.out.println("경기 지역에서 가져오고 저장된 총 이벤트 :" + totalEvents);

        } catch (Exception e) {
            System.err.println("경기 지역에 대한 이벤트를 가져오고 저장하는 중 오류가 발생했습니다. : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fetchAndSaveLocalJeonnamEvents(int numOfRows) {
        try {
            String region = "jeonnam";
            String pageNo = "1";     // 첫 페이지부터 시작

            // 전라남도의 시군구 코드 목록
            int[] sigunguCodes = {
                    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22
            };

            int totalEvents = 0;

            // 각 시군구에 대해 데이터를 가져오는 반복문
            for (int sigunguCode : sigunguCodes) {
                List<LocalBase> events = localBasedService.fetchAndSaveEvents(region, sigunguCode, Integer.parseInt(String.valueOf(numOfRows)), pageNo);

                // 중복 데이터 필터링 및 저장
                List<LocalBase> filteredEvents = new ArrayList<>();
                for (LocalBase event : events) {
                    // 데이터베이스에 존재하지 않는 경우만 저장
                    if (!localBasedRepository.existsByContentid(event.getContentid())) {
                        filteredEvents.add(event);
                    }
                }

                if (!filteredEvents.isEmpty()) {
                    localBasedRepository.saveAll(filteredEvents); // 중복 필터링된 데이터만 저장
                    System.out.println("데이터 저장 : " + filteredEvents.size() + "지역의 데이터 : " + region + ", 시/군/구 : " + sigunguCode);
                    totalEvents += filteredEvents.size();
                } else {
                    System.out.println("지역의 새로운 데이터를 받아오지 못함 : : " + region + ", 시/군/구 : " + sigunguCode);
                }
            }

            System.out.println("경기 지역에서 가져오고 저장된 총 이벤트 :" + totalEvents);

        } catch (Exception e) {
            System.err.println("경기 지역에 대한 이벤트를 가져오고 저장하는 중 오류가 발생했습니다. : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fetchAndSaveLocalJeonbukEvents(int numOfRows) {
        try {
            String region = "jeonbuk";
            String pageNo = "1";     // 첫 페이지부터 시작

            // 전라북도의 시군구 코드 목록
            int[] sigunguCodes = {
                    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14
            };

            int totalEvents = 0;

            // 각 시군구에 대해 데이터를 가져오는 반복문
            for (int sigunguCode : sigunguCodes) {
                List<LocalBase> events = localBasedService.fetchAndSaveEvents(region, sigunguCode, Integer.parseInt(String.valueOf(numOfRows)), pageNo);

                // 중복 데이터 필터링 및 저장
                List<LocalBase> filteredEvents = new ArrayList<>();
                for (LocalBase event : events) {
                    // 데이터베이스에 존재하지 않는 경우만 저장
                    if (!localBasedRepository.existsByContentid(event.getContentid())) {
                        filteredEvents.add(event);
                    }
                }

                if (!filteredEvents.isEmpty()) {
                    localBasedRepository.saveAll(filteredEvents); // 중복 필터링된 데이터만 저장
                    System.out.println("데이터 저장 : " + filteredEvents.size() + "지역의 데이터 : " + region + ", 시/군/구 : " + sigunguCode);
                    totalEvents += filteredEvents.size();
                } else {
                    System.out.println("지역의 새로운 데이터를 받아오지 못함 : : " + region + ", 시/군/구 : " + sigunguCode);
                }
            }

            System.out.println("경기 지역에서 가져오고 저장된 총 이벤트 :" + totalEvents);

        } catch (Exception e) {
            System.err.println("경기 지역에 대한 이벤트를 가져오고 저장하는 중 오류가 발생했습니다. : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fetchAndSaveLocalUlsanEvents(int numOfRows) {
        try {
            String region = "ulsan";
            String pageNo = "1";     // 첫 페이지부터 시작

            // 울산광역시의 시군구 코드 목록
            int[] sigunguCodes = {
                    1, 2, 3, 4, 5
            };

            int totalEvents = 0;

            // 각 시군구에 대해 데이터를 가져오는 반복문
            for (int sigunguCode : sigunguCodes) {
                List<LocalBase> events = localBasedService.fetchAndSaveEvents(region, sigunguCode, Integer.parseInt(String.valueOf(numOfRows)), pageNo);

                // 중복 데이터 필터링 및 저장
                List<LocalBase> filteredEvents = new ArrayList<>();
                for (LocalBase event : events) {
                    // 데이터베이스에 존재하지 않는 경우만 저장
                    if (!localBasedRepository.existsByContentid(event.getContentid())) {
                        filteredEvents.add(event);
                    }
                }

                if (!filteredEvents.isEmpty()) {
                    localBasedRepository.saveAll(filteredEvents); // 중복 필터링된 데이터만 저장
                    System.out.println("데이터 저장 : " + filteredEvents.size() + "지역의 데이터 : " + region + ", 시/군/구 : " + sigunguCode);
                    totalEvents += filteredEvents.size();
                } else {
                    System.out.println("지역의 새로운 데이터를 받아오지 못함 : : " + region + ", 시/군/구 : " + sigunguCode);
                }
            }

            System.out.println("경기 지역에서 가져오고 저장된 총 이벤트 :" + totalEvents);

        } catch (Exception e) {
            System.err.println("경기 지역에 대한 이벤트를 가져오고 저장하는 중 오류가 발생했습니다. : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fetchAndSaveLocalSejongEvents(int numOfRows) {
        try {
            String region = "sejong";
            String pageNo = "1";     // 첫 페이지부터 시작

            // 세종특별자치시의 시군구 코드 목록
            int[] sigunguCodes = {
                    1
            };

            int totalEvents = 0;

            // 각 시군구에 대해 데이터를 가져오는 반복문
            for (int sigunguCode : sigunguCodes) {
                List<LocalBase> events = localBasedService.fetchAndSaveEvents(region, sigunguCode, Integer.parseInt(String.valueOf(numOfRows)), pageNo);

                // 중복 데이터 필터링 및 저장
                List<LocalBase> filteredEvents = new ArrayList<>();
                for (LocalBase event : events) {
                    // 데이터베이스에 존재하지 않는 경우만 저장
                    if (!localBasedRepository.existsByContentid(event.getContentid())) {
                        filteredEvents.add(event);
                    }
                }

                if (!filteredEvents.isEmpty()) {
                    localBasedRepository.saveAll(filteredEvents); // 중복 필터링된 데이터만 저장
                    System.out.println("데이터 저장 : " + filteredEvents.size() + "지역의 데이터 : " + region + ", 시/군/구 : " + sigunguCode);
                    totalEvents += filteredEvents.size();
                } else {
                    System.out.println("지역의 새로운 데이터를 받아오지 못함 : : " + region + ", 시/군/구 : " + sigunguCode);
                }
            }

            System.out.println("경기 지역에서 가져오고 저장된 총 이벤트 :" + totalEvents);

        } catch (Exception e) {
            System.err.println("경기 지역에 대한 이벤트를 가져오고 저장하는 중 오류가 발생했습니다. : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fetchAndSaveLocalJejuEvents(int numOfRows) {
        try {
            String region = "jeju";
            String pageNo = "1";     // 첫 페이지부터 시작

            // 제주특별자치도의 시군구 코드 목록
            int[] sigunguCodes = {
                    1, 2, 3, 4
            };

            int totalEvents = 0;

            // 각 시군구에 대해 데이터를 가져오는 반복문
            for (int sigunguCode : sigunguCodes) {
                List<LocalBase> events = localBasedService.fetchAndSaveEvents(region, sigunguCode, Integer.parseInt(String.valueOf(numOfRows)), pageNo);

                // 중복 데이터 필터링 및 저장
                List<LocalBase> filteredEvents = new ArrayList<>();
                for (LocalBase event : events) {
                    // 데이터베이스에 존재하지 않는 경우만 저장
                    if (!localBasedRepository.existsByContentid(event.getContentid())) {
                        filteredEvents.add(event);
                    }
                }

                if (!filteredEvents.isEmpty()) {
                    localBasedRepository.saveAll(filteredEvents); // 중복 필터링된 데이터만 저장
                    System.out.println("데이터 저장 : " + filteredEvents.size() + "지역의 데이터 : " + region + ", 시/군/구 : " + sigunguCode);
                    totalEvents += filteredEvents.size();
                } else {
                    System.out.println("지역의 새로운 데이터를 받아오지 못함 : : " + region + ", 시/군/구 : " + sigunguCode);
                }
            }

            System.out.println("경기 지역에서 가져오고 저장된 총 이벤트 :" + totalEvents);

        } catch (Exception e) {
            System.err.println("경기 지역에 대한 이벤트를 가져오고 저장하는 중 오류가 발생했습니다. : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fetchAndSaveLocalGangwonEvents(int numOfRows) {
        try {
            String region = "gangwon"; // 경기도 지역을 지정
            String pageNo = "1"; // 첫 페이지부터 시작

            // 강원특별자치도의 시군구 코드 목록
            int[] sigunguCodes = {
                    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18
            };


            int totalEvents = 0;

            // 각 시군구에 대해 데이터를 가져오는 반복문
            for (int sigunguCode : sigunguCodes) {
                List<LocalBase> events = localBasedService.fetchAndSaveEvents(region, sigunguCode, numOfRows, pageNo);

                // 중복 데이터 필터링 및 저장
                List<LocalBase> filteredEvents = new ArrayList<>();
                for (LocalBase event : events) {
                    // 데이터베이스에 존재하지 않는 경우만 저장
                    if (!localBasedRepository.existsByContentid(event.getContentid())) {
                        filteredEvents.add(event);
                    }
                }

                if (!filteredEvents.isEmpty()) {
                    localBasedRepository.saveAll(filteredEvents); // 중복 필터링된 데이터만 저장
                    System.out.println("데이터 저장 : " + filteredEvents.size() + "지역의 데이터 : " + region + ", 시/군/구 : " + sigunguCode);
                    totalEvents += filteredEvents.size();
                } else {
                    System.out.println("지역의 새로운 데이터를 받아오지 못함 : : " + region + ", 시/군/구 : " + sigunguCode);
                }
            }

            System.out.println("경기 지역에서 가져오고 저장된 총 이벤트 :" + totalEvents);

        } catch (Exception e) {
            System.err.println("경기 지역에 대한 이벤트를 가져오고 저장하는 중 오류가 발생했습니다. : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fetchAndSaveLocalBusanEvents(int numOfRows) {
        try {
            String region = "busan"; // 경기도 지역을 지정
            String pageNo = "1"; // 첫 페이지부터 시작

            // 부산광역시의 시군구 코드 목록
            int[] sigunguCodes = {
                    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16
            };


            int totalEvents = 0;

            // 각 시군구에 대해 데이터를 가져오는 반복문
            for (int sigunguCode : sigunguCodes) {
                List<LocalBase> events = localBasedService.fetchAndSaveEvents(region, sigunguCode, numOfRows, pageNo);

                // 중복 데이터 필터링 및 저장
                List<LocalBase> filteredEvents = new ArrayList<>();
                for (LocalBase event : events) {
                    // 데이터베이스에 존재하지 않는 경우만 저장
                    if (!localBasedRepository.existsByContentid(event.getContentid())) {
                        filteredEvents.add(event);
                    }
                }

                if (!filteredEvents.isEmpty()) {
                    localBasedRepository.saveAll(filteredEvents); // 중복 필터링된 데이터만 저장
                    System.out.println("데이터 저장 : " + filteredEvents.size() + "지역의 데이터 : " + region + ", 시/군/구 : " + sigunguCode);
                    totalEvents += filteredEvents.size();
                } else {
                    System.out.println("지역의 새로운 데이터를 받아오지 못함 : : " + region + ", 시/군/구 : " + sigunguCode);
                }
            }

            System.out.println("경기 지역에서 가져오고 저장된 총 이벤트 :" + totalEvents);

        } catch (Exception e) {
            System.err.println("경기 지역에 대한 이벤트를 가져오고 저장하는 중 오류가 발생했습니다. : " + e.getMessage());
            e.printStackTrace();
        }
    }

}