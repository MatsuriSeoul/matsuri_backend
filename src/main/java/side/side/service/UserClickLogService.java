package side.side.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import side.side.model.UserClickLog;
import side.side.model.UserInfo;
import side.side.repository.*;

import java.util.*;

@Service
public class UserClickLogService {

    @Autowired
    private UserClickLogRepository userClickLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TouristAttractionRepository touristAttractionRepository;
    @Autowired
    private CulturalFacilityRepository culturalFacilityRepository;
    @Autowired
    private LocalEventRepository localEventRepository;
    @Autowired
    private TravelCourseDetailRepository travelCourseDetailRepository;
    @Autowired
    private LeisureSportsEventRepository leisureSportsEventRepository;
    @Autowired
    private ShoppingEventRepository shoppingEventRepository;
    @Autowired
    private FoodEventRepository foodEventRepository;
    @Autowired
    private TourEventRepository tourEventRepository;


    // 사용자 클릭 로그를 저장하는 메서드
    public void logUserClick(Long userId, String contentid, String contenttypeid) {
        // 사용자 정보 가져오기
        UserInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 새로운 클릭 로그 생성
        UserClickLog log = new UserClickLog();
        log.setUser(user); // 사용자 객체를 설정
        log.setContentid(contentid);
        log.setContenttypeid(contenttypeid);

        // 클릭 로그 DB에 저장
        userClickLogRepository.save(log);
    }

    // 특정 사용자의 가장 많이 조회한 카테고리 반환
    public String findMostViewedCategoryByUser(Long userId) {
        // 여기서는 클릭 로그에서 가장 많이 조회한 카테고리(contenttypeid)를 찾는 로직을 구현
        return userClickLogRepository.findTopCategoryByUserId(userId).get(0)[0].toString();
    }

    //  인기있는 행사 메서드에서 새로운 메서드 호출
    public List<Map<String, Object>> findTopContentByAllUsers() {
        List<Object[]> topContentLogs = userClickLogRepository.findTopContentByAllUsers();
        List<Map<String, Object>> topContentData = new ArrayList<>();

        for (Object[] log : topContentLogs) {
            String contentid = log[0].toString();
            String contenttypeid = log[1].toString();

            // 새로 추가된 메서드 호출
            List<Map<String, Object>> categoryData = getCategoryDataById(contenttypeid, contentid);
            if (!categoryData.isEmpty()) {
                topContentData.addAll(categoryData);
            }
        }

        return topContentData;
    }

    // 카테고리 데이터를 반환하는 메서드
    public List<Map<String, Object>> getCategoryData(String contenttypeid) {
        List<Map<String, Object>> data;
        switch (contenttypeid) {
            case "12":
                return touristAttractionRepository.findTopTouristAttractions();  // 관광지 데이터 조회
            case "14":
                return culturalFacilityRepository.findTopCulturalFacilities();  // 문화시설 데이터 조회
            case "15":
                return tourEventRepository.findTopEvents();  // 행사 데이터 조회
            case "25":
                return travelCourseDetailRepository.findTopTravelCourses();  // 여행코스 데이터 조회
            case "28":
                return leisureSportsEventRepository.findTopLeisureSports();  // 레포츠 데이터 조회
            case "32":
                return localEventRepository.findTopAccommodations();  // 숙박 데이터 조회
            case "38":
                return shoppingEventRepository.findTopShoppingEvents();  // 쇼핑 데이터 조회
            case "39":
                return foodEventRepository.findTopFoodEvents();  // 음식 데이터 조회
            // 다른 카테고리들도 같은 방식으로 추가
            default:
                data = List.of();  // 해당 카테고리가 없을 경우 빈 리스트 반환
        }
        // 데이터 섞기
        Collections.shuffle(data, new Random());
        return data;
    }
    // 인기있는 행사 추천
    public List<Map<String, Object>> getCategoryDataById(String contenttypeid, String contentid) {
        switch (contenttypeid) {
            case "12":
                return touristAttractionRepository.findTopTouristAttractionsByContentid(contentid);
            case "14":
                return culturalFacilityRepository.findTopCulturalFacilitiesByContentid(contentid);
            case "15":
                return tourEventRepository.findTopTourEventsByContentid(contentid);
            case "25":
                return travelCourseDetailRepository.findTopTravelCoursesByContentid(contentid);
            case "28":
                return leisureSportsEventRepository.findTopLeisureSportsEventsByContentid(contentid);
            case "32":
                return localEventRepository.findTopLocalEventsByContentid(contentid);
            case "38":
                return shoppingEventRepository.findTopShoppingEventsByContentid(contentid);
            case "39":
                return foodEventRepository.findTopFoodEventsByContentid(contentid);
            default:
                return List.of();
        }
    }

}