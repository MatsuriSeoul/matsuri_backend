package side.side.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import side.side.model.*;
import side.side.model.DTO.EventDTO;
import side.side.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class EventService {
    private static final Logger logger = Logger.getLogger(EventService.class.getName());

    @Autowired
    private GyeonggiEventRepository gyeonggiEventRepository;

    @Autowired
    private SeoulEventRepository seoulEventRepository;

    @Autowired
    private TourEventDetailRepository tourEventDetailRepository;

    @Autowired
    private ShoppingEventService shoppingEventService;
    @Autowired
    private FoodEventService foodEventService;
    @Autowired
    private LocalEventService localEventService;
    @Autowired
    private CulturalFacilityService culturalFacilityService;
    @Autowired
    private TravelCourseService travelCourseService;
    @Autowired
    private LeisureSportsEventService leisureSportsEventService;
    @Autowired
    private TouristAttractionsService touristAttractionsService;
    @Autowired
    private TourEventService tourEventService;
    @Autowired
    private ShoppingEventRepository shoppingEventRepository;
    @Autowired
    private FoodEventRepository foodEventRepository;
    @Autowired
    private LocalEventRepository localEventRepository;
    @Autowired
    private CulturalFacilityRepository culturalFacilityRepository;
    @Autowired
    private TravelCourseRepository travelCourseRepository;
    @Autowired
    private LeisureSportsEventRepository leisureSportsEventRepository;
    @Autowired
    private TouristAttractionRepository touristAttractionRepository;
    @Autowired
    private TourEventRepository tourEventRepository;
    @Autowired
    private ShoppingEventDetailRepository shoppingEventDetailRepository;
    @Autowired
    private FoodEventDetailRepository foodEventDetailRepository;
    @Autowired
    private LocalEventDetailRepository localEventDetailRepository;
    @Autowired
    private CulturalFacilityDetailRepository culturalFacilityDetailRepository;
    @Autowired
    private TravelCourseDetailRepository travelCourseDetailRepository;
    @Autowired
    private LeisureSportsEventDetailRepository leisureSportsEventDetailRepository;
    @Autowired
    private TouristAttractionDetailRepository touristAttractionDetailRepository;



    // 키 값 절대 건들이면 안됨
    private final String gyeonggiApiKey;

    private final  String seoulApiKey;

    private final String serviceKey;

    public EventService() {
        Dotenv dotenv = Dotenv.load();
        this.gyeonggiApiKey = dotenv.get("GYEONGGI_API_KEY");
        this.seoulApiKey = dotenv.get("SEOUL_API_KEY");
        this.serviceKey = dotenv.get("SERVICE_API_KEY");
    }

//    // 경기도 행사 API
//    public void fetchAndSaveGyeonggiEvents() {
//        int pageSize = 50;  // 가져올 데이터 개수를 10개로 설정
//        int startIndex = 1;
//        boolean moreData = true;
//        RestTemplate restTemplate = new RestTemplate();
//
//        while (moreData) {
//            String url = String.format("https://openapi.gg.go.kr/GGCULTUREVENTSTUS?KEY=%s&Type=json&pIndex=%d&pSize=%d",
//                    gyeonggiApiKey, startIndex, pageSize);
//            try {
//                String response = restTemplate.getForObject(url, String.class);
//                System.out.println("경기도 API 응답" + response);
//
//                if (response.startsWith("<")) {
//                    throw new IllegalArgumentException("존재하지 않는 응답 API");
//                }
//
//                ObjectMapper objectMapper = new ObjectMapper();
//                JsonNode rootNode = objectMapper.readTree(response);
//                JsonNode dataNode = rootNode.path("GGCULTUREVENTSTUS").path(1).path("row");
//
//                if (dataNode.isArray()) {
//                    List<GyeonggiEvent> events = new ArrayList<>();
//                    for (JsonNode node : dataNode) {
//                        GyeonggiEvent event = new GyeonggiEvent();
//                        event.setInstNm(node.path("INST_NM").asText());
//                        event.setTitle(node.path("TITLE").asText());
//                        event.setCategoryNm(node.path("CATEGORY_NM").asText());
//                        event.setUrl(node.path("URL").asText());
//                        event.setImageUrl(node.path("IMAGE_URL").asText());
//                        event.setBeginDe(node.path("BEGIN_DE").asText());
//                        event.setEndDe(node.path("END_DE").asText());
//                        event.setAddr(node.path("ADDR").asText());
//                        event.setEventTmInfo(node.path("EVENT_TM_INFO").asText());
//                        event.setPartcptExpnInfo(node.path("PARTCPT_EXPN_INFO").asText());
//                        event.setTelnoInfo(node.path("TELNO_INFO").asText());
//                        event.setHostInstNm(node.path("HOST_INST_NM").asText());
//                        event.setHmpgUrl(node.path("HMPG_URL").asText());
//                        event.setWritngDe(node.path("WRITNG_DE").asText());
//                        events.add(event);
//                    }
//                    gyeonggiEventRepository.saveAll(events);
//
//                    // 10개씩 가져오는 것으로 설정했으므로 한 번만 가져오고 종료
//                    moreData = false;
//                } else {
//                    moreData = false;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                moreData = false;
//            }
//        }
//    }

    // 서울 행사 API
    public void fetchAndSaveSeoulEvents() {
        int pageSize = 50;  // 데이터를 10개만 가져오기 위해 pageSize를 10으로 설정
        int startIndex = 1;
        RestTemplate restTemplate = new RestTemplate();

        String url = String.format("http://openAPI.seoul.go.kr:8088/%s/json/ListPublicReservationCulture/%d/%d/",
                seoulApiKey, startIndex, startIndex + pageSize - 1);
        try {
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("서울 API 응답" + response);

            if (response.startsWith("<")) {
                throw new IllegalArgumentException("JSON 응답 API 안됨");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode dataNode = rootNode.path("ListPublicReservationCulture").path("row");

                // 최대 10개의 데이터를 가져오기 위해, API 호출 후 처음 10개의 데이터만 추가

                if (dataNode.isArray()) {
                    List<SeoulEvent> events = new ArrayList<>();
                for (JsonNode node : dataNode) {
                    SeoulEvent event = new SeoulEvent();
                    event.setGubun(node.path("GUBUN").asText());
                    event.setSvcid(node.path("SVCID").asText());
                    event.setMaxclassnm(node.path("MAXCLASSNM").asText());
                    event.setMinclassnm(node.path("MINCLASSNM").asText());
                    event.setSvcstatnm(node.path("SVCSTATNM").asText());
                    event.setSvcnm(node.path("SVCNM").asText());
                    event.setPayatnm(node.path("PAYATNM").asText());
                    event.setPlacenm(node.path("PLACENM").asText());
                    event.setUsetgtinfo(node.path("USETGTINFO").asText());
                    event.setSvcurl(node.path("SVCURL").asText());
                    event.setX(node.path("X").asText());
                    event.setY(node.path("Y").asText());
                    event.setSvcopnbgndt(node.path("SVCOPNBGNDT").asText());
                    event.setSvcopnenddt(node.path("SVCOPNENDDT").asText());
                    event.setRcptbgndt(node.path("RCPTBGNDT").asText());
                    event.setRcptenddt(node.path("RCPTENDDT").asText());
                    event.setAreanm(node.path("AREANM").asText());
                    event.setImgurl(node.path("IMGURL").asText());
                    event.setDtlcont(node.path("DTLCONT").asText());
                    event.setTelno(node.path("TELNO").asText());
                    event.setVMin(node.path("V_MIN").asText());
                    event.setVMax(node.path("V_MAX").asText());
                    event.setRevstddaynm(node.path("REVSTDDAYNM").asText());
                    event.setRevstdday(node.path("REVSTDDAY").asText());
                    events.add(event);

                    // 데이터를 10개만 가져오기 위해 조건 추가
                    if (events.size() >= 50) break;
                }
                seoulEventRepository.saveAll(events);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 한국관광공사_국문 관광정보 서비스_GW API / TourEvent
    @Transactional(propagation = Propagation.REQUIRED)
    public List<TourEvent> fetchAndSaveEvents(String numOfRows, String pageNo, String eventStartDate) {
        List<TourEvent> allEvents = new ArrayList<>();
        boolean moreData = true;
        numOfRows = "15";  // 호출되는 데이터의 개수를 10개로 제한

        RestTemplate restTemplate = new RestTemplate();
        String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/searchFestival1")
                .queryParam("serviceKey", serviceKey)
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", pageNo)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "AppTest")
                .queryParam("_type", "json")
                .queryParam("eventStartDate", eventStartDate)
                .build()
                .toUriString();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");

                if (itemsNode.isArray()) {
                    for (JsonNode node : itemsNode) {
                        TourEvent event = new TourEvent();
                        event.setTitle(node.path("title").asText());
                        event.setAddr1(node.path("addr1").asText());
                        event.setEventstartdate(node.path("eventstartdate").asText());
                        event.setEventenddate(node.path("eventenddate").asText());
                        event.setFirstimage(node.path("firstimage").asText());
                        event.setCat1(node.path("cat1").asText());
                        event.setCat2(node.path("cat2").asText());
                        event.setCat3(node.path("cat3").asText());
                        event.setMapx(node.path("mapx").asText());
                        event.setMapy(node.path("mapy").asText());
                        event.setContentid(node.path("contentid").asText());
                        event.setContenttypeid(node.path("contenttypeid").asText());

                        // 데이터에 락 걸기
                        Optional<TourEvent> existingDetail = tourEventRepository.findByContentidForUpdate(event.getContentid());
                        if (existingDetail.isPresent()) {
                            continue;
                        }

                        // Upsert 사용하여 데이터 삽입 또는 업데이트
                        tourEventRepository.upsertTourEvent(
                                event.getContentid(),
                                event.getTitle(),
                                event.getAddr1(),
                                event.getEventstartdate(),
                                event.getEventenddate(),
                                event.getFirstimage(),
                                event.getCat1(),
                                event.getCat2(),
                                event.getCat3(),
                                event.getMapx(),
                                event.getMapy(),
                                event.getContenttypeid()
                        );

                        allEvents.add(event);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return allEvents;
    }


        //모든 데이터 요청
//        while (moreData) {
//            String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/searchFestival1")
//                    .queryParam("serviceKey", serviceKey)
//                    .queryParam("numOfRows", numOfRows)
//                    .queryParam("pageNo", pageNo)
//                    .queryParam("MobileOS", "ETC")
//                    .queryParam("MobileApp", "AppTest")
//                    .queryParam("_type", "json")
//                    .queryParam("eventStartDate", eventStartDate)
//                    .build()
//                    .toUriString();
//
//            try {
//                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
//                if (response.getStatusCode().is2xxSuccessful()) {
//                    ObjectMapper objectMapper = new ObjectMapper();
//                    JsonNode rootNode = objectMapper.readTree(response.getBody());
//                    JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");
//
//                    if (itemsNode.isArray()) {
//                        List<TourEvent> events = new ArrayList<>();
//                        for (JsonNode node : itemsNode) {
//                            TourEvent event = new TourEvent();
//                            event.setTitle(node.path("title").asText());
//                            event.setAddr1(node.path("addr1").asText());
//                            event.setEventstartdate(node.path("eventstartdate").asText());
//                            event.setEventenddate(node.path("eventenddate").asText());
//                            event.setFirstimage(node.path("firstimage").asText());
//                            event.setCat1(node.path("cat1").asText());
//                            event.setCat2(node.path("cat2").asText());
//                            event.setCat3(node.path("cat3").asText());
//                            event.setContentid(node.path("contentid").asText());
//                            event.setContenttypeid(node.path("contenttypeid").asText());
//                            events.add(event);
//                        }
//                        tourEventRepository.saveAll(events);
//                        allEvents.addAll(events);
//
//                        if (itemsNode.size() < Integer.parseInt(numOfRows)) {
//                            moreData = false;
//                        } else {
//                            pageNo = String.valueOf(Integer.parseInt(pageNo) + 1);
//                        }
//                    } else {
//                        moreData = false;
//                    }
//                } else {
//                    moreData = false;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                moreData = false;
//            }
//        }
//
//        return allEvents;
//    }


    //행사의 상세 정보
    @Transactional(propagation = Propagation.REQUIRED)
    public void fetchAndSaveEventDetail(String contentid) {
        RestTemplate restTemplate = new RestTemplate();

        String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/detailCommon1")
                .queryParam("serviceKey", serviceKey)
                .queryParam("contentId", contentid)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "AppTest")
                .queryParam("defaultYN", "Y")
                .queryParam("addrinfoYN", "Y")
                .queryParam("overviewYN", "Y")
                .queryParam("mapinfoYN", "Y") // 좌표 정보 요청
                .queryParam("_type", "json")
                .build()
                .toUriString();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode itemNode = rootNode.path("response").path("body").path("items").path("item").get(0);

                if (itemNode.isMissingNode()) {
                    return;
                }

                TourEventDetail eventDetail = new TourEventDetail();
                eventDetail.setContentid(itemNode.path("contentid").asText());
                eventDetail.setContenttypeid(itemNode.path("contenttypeid").asText());
                eventDetail.setBooktour(itemNode.path("booktour").asText());
                eventDetail.setCreatedtime(itemNode.path("createdtime").asText());
                eventDetail.setHomepage(itemNode.path("homepage").asText());
                eventDetail.setModifiedtime(itemNode.path("modifiedtime").asText());
                eventDetail.setTel(itemNode.path("tel").asText());
                eventDetail.setTelname(itemNode.path("telname").asText());
                eventDetail.setTitle(itemNode.path("title").asText());
                eventDetail.setFirstimage(itemNode.path("firstimage").asText());
                eventDetail.setFirstimage2(itemNode.path("firstimage2").asText());
                eventDetail.setAreacode(itemNode.path("areacode").asText());
                eventDetail.setSigungucode(itemNode.path("sigungucode").asText());
                eventDetail.setCat1(itemNode.path("cat1").asText());
                eventDetail.setCat2(itemNode.path("cat2").asText());
                eventDetail.setCat3(itemNode.path("cat3").asText());
                eventDetail.setAddr1(itemNode.path("addr1").asText());
                eventDetail.setAddr2(itemNode.path("addr2").asText());
                eventDetail.setZipcode(itemNode.path("zipcode").asText());
                eventDetail.setMapx(itemNode.path("mapx").asText());
                eventDetail.setMapy(itemNode.path("mapy").asText());
                eventDetail.setMlevel(itemNode.path("mlevel").asText());
                eventDetail.setOverview(itemNode.path("overview").asText());

                // 데이터에 락 걸기
                Optional<TourEventDetail> existingDetail = tourEventDetailRepository.findByContentidForUpdate(contentid);
                if (existingDetail.isPresent()) {
                    return;
                }

                // Upsert 사용하여 데이터 삽입 또는 업데이트
                tourEventDetailRepository.upsertTourEventDetail(
                        eventDetail.getContentid(),
                        eventDetail.getContenttypeid(),
                        eventDetail.getBooktour(),
                        eventDetail.getCreatedtime(),
                        eventDetail.getHomepage(),
                        eventDetail.getModifiedtime(),
                        eventDetail.getTel(),
                        eventDetail.getTelname(),
                        eventDetail.getTitle(),
                        eventDetail.getFirstimage(),
                        eventDetail.getFirstimage2(),
                        eventDetail.getAreacode(),
                        eventDetail.getSigungucode(),
                        eventDetail.getCat1(),
                        eventDetail.getCat2(),
                        eventDetail.getCat3(),
                        eventDetail.getAddr1(),
                        eventDetail.getAddr2(),
                        eventDetail.getZipcode(),
                        eventDetail.getMapx(),
                        eventDetail.getMapy(),
                        eventDetail.getMlevel(),
                        eventDetail.getOverview()
                );
            } else {
                logger.warning("contentID에 따른 데이터 불러오지 못함 : " + contentid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //소개 정보 API
    public JsonNode fetchIntroInfoFromApi(String contentid, String contenttypeid) {
        RestTemplate restTemplate = new RestTemplate();

        String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/detailIntro1")
                .queryParam("serviceKey", serviceKey)
                .queryParam("contentId", contentid)
                .queryParam("contentTypeId", contenttypeid)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "AppTest")
                .queryParam("_type", "json")
                .build()
                .toUriString();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");

                if (itemsNode.isArray() && itemsNode.size() > 0) {
                    return itemsNode.get(0);
                } else {
                    logger.warning("해당 contentId에 대한 소개 정보가 없습니다: " + contentid);
                    return null;
                }
            } else {
                logger.warning("소개 정보를 가져오지 못했습니다: contentId = " + contentid);
                return null;
            }
        } catch (Exception e) {
            logger.severe("contentId: " + contentid + "에 대한 소개 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    //이미지 정보 조회 API
    public JsonNode fetchImagesFromApi(String contentid) {
        RestTemplate restTemplate = new RestTemplate();

        String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/detailImage1")
                .queryParam("serviceKey", serviceKey)
                .queryParam("contentId", contentid)
                .queryParam("imageYN", "Y")
                .queryParam("subImageYN", "Y")
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "AppTest")
                .queryParam("_type", "json")
                .build()
                .toUriString();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");

                if (itemsNode.isArray() && itemsNode.size() > 0) {
                    return itemsNode;
                } else {
                    logger.warning("해당 contentId에 대한 이미지를 찾을 수 없습니다: " + contentid);
                    return null;
                }
            } else {
                logger.warning("이미지 정보를 가져오지 못했습니다: contentId = " + contentid);
                return null;
            }
        } catch (Exception e) {
            logger.severe("contentId: " + contentid + "에 대한 이미지 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    // '서울특별시'에 해당하는 쇼핑 이벤트 가져오기
    public List<TourEvent> getTourEventsByRegion(String region) {
        return tourEventRepository.findAll().stream()
                .filter(event -> event.getAddr1().contains(region))
                .collect(Collectors.toList());
    }

    //데이터베이스에서 행사 상세 정보 추출
    @Transactional
    public TourEventDetail getEventDetailFromDB(String contentid) {
        return tourEventDetailRepository.findByContentid(contentid);
    }

    public List<String> getAllContentIds() {
        return tourEventRepository.findAll().stream()
                .map(event -> event.getContentid())
                .collect(Collectors.toList());
    }


    // 서울 특정 카테고리별 이벤트 조회
    public List<?> fetchEventsByCategory(String region, String category) {
        if (region.equals("서울특별시")) {
            switch (category) {
                case "쇼핑":
                    return shoppingEventService.getShoppingEventsByCategory(region);
                case "음식":
                    return foodEventService.getFoodEventsByCategory(region);
                case "숙박":
                    return localEventService.getLocalEventsByRegion(region);
                case "문화시설":
                    return culturalFacilityService.getCulturalFacilityByRegion(region);
                case "여행코스":
                    return travelCourseService.getTravelCourseByRegion(region);
                case "레포츠":
                    return leisureSportsEventService.getLeisureSportsByRegion(region);
                case "관광지":
                    return touristAttractionsService.getTouristAttractionByRegion(region);

                default:
                    return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }
    // 경기 이벤트 필터링 로직
    public List<GyeonggiEvent> getGyeonggiEventsByCategory(String category) {
        if (category != null && !category.isEmpty()) {
            // category_nm 필드를 기준으로 필터링
            return gyeonggiEventRepository.findByCategoryNm(category);
        }
        return gyeonggiEventRepository.findAll();  // 카테고리가 없으면 전체 조회
    }

    // 서울 이벤트 필터링 로직
    public List<SeoulEvent> getSeoulEventsByCategory(String category) {
        if (category != null && !category.isEmpty()) {
            // minclassnm 필드를 기준으로 필터링
            return seoulEventRepository.findByMinclassnm(category);
        }
        return seoulEventRepository.findAll();  // 카테고리가 없으면 전체 조회
    }

    // 월과 카테고리에 맞는 데이터를 조회
    public List<SeoulEvent> getSeoulEventsByMonthAndCategory(String month, String category) {
        // 월 정보가 있는 경우 "MM" 형식으로 생성
        String beginDatePattern = String.format("%02d", Integer.parseInt(month));

        // 리포지토리 메소드 호출하여 월과 카테고리에 맞는 데이터 조회
        return seoulEventRepository.findByCategoryAndMonth(beginDatePattern, category);
    }

    // 카테고리에 맞는 모든 데이터를 조회
    public List<SeoulEvent> getSeoulEventsByCategoryMonthNull(String category) {
        // 리포지토리 메소드 호출하여 해당 카테고리의 모든 데이터 조회
        return seoulEventRepository.findByEventInCategory(category);
    }


    public List<TourEvent> getRandomEventsByRegion(String region) {
        return tourEventRepository.findRandomEventsByRegion(region);
    }

    // 경기도 무료 행사 가져오기
    public List<GyeonggiEvent> getGyeonggiFreeEvents() {
        return gyeonggiEventRepository.findFreeEventsInGyeonggi();
    }

    // 서울특별시 무료 행사 가져오기
    public List<SeoulEvent> getSeoulFreeEvents() {
        return seoulEventRepository.findFreeEventsInSeoul();
    }

    // 경기도 유료 행사 가져오기
    public List<GyeonggiEvent> getGyeonggiPaidEvents() {
        return gyeonggiEventRepository.findPaidEventsInGyeonggi();
    }

    // 서울특별시 유료 행사 가져오기
    public List<SeoulEvent> getSeoulPaidEvents() {
        return seoulEventRepository.findPaidEventsInSeoul();
    }


    // 경기도 진행 중 및 예정된 행사 가져오기
    public List<GyeonggiEvent> getGyeonggiScheduledEvents() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return gyeonggiEventRepository.findScheduledEvents(today);
    }

    // 서울특별시 진행 중 및 예정된 행사 가져오기
    public List<SeoulEvent> getSeoulScheduledEvents() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return seoulEventRepository.findScheduledEvents(today);
    }
    // 유사한 여행지 정보 가져오기
    public List<TourEvent> getSimilarTourEvent(String contenttypeid) {
        return tourEventRepository.findByContenttypeid(contenttypeid);
    }

    // 월과 카테고리에 맞는 데이터를 조회
    public List<GyeonggiEvent> getGyeonggiEventsByMonthAndCategory(String month, String category) {
        // 월 정보가 있는 경우 "MM" 형식으로 생성
        String beginDatePattern = String.format("%02d", Integer.parseInt(month));

        // 리포지토리 메소드 호출하여 월과 카테고리에 맞는 데이터 조회
        return gyeonggiEventRepository.findByCategoryAndMonth(beginDatePattern, category);
    }

    // 카테고리에 맞는 모든 데이터를 조회
    public List<GyeonggiEvent> getGyeonggiEventsByCategoryMonthNull(String category) {
        // 리포지토리 메소드 호출하여 해당 카테고리의 모든 데이터 조회
        return gyeonggiEventRepository.findByEventInCategory(category);
    }


    @Transactional
    public EventDTO findEventDetailFromAllSources(String contentId) {
        String title = null;
        String firstImage = null;
        String imgurl = null;
        String imageUrl = null;
        String overview = null;
        String contenttypeid = null;

        // CulturalFacilityDetail 조회
        CulturalFacilityDetail facilityDetail = culturalFacilityDetailRepository.findByContentid(contentId);
        if (facilityDetail != null) {
            CulturalFacility facility = culturalFacilityRepository.findFirstByContentid(contentId).orElse(null);

            title = facilityDetail.getTitle();
            overview = facilityDetail.getOverview();
            firstImage = (facility != null) ? facility.getFirstimage() : null; // 이미지
            contenttypeid = "14";
            return new EventDTO(contentId, contenttypeid, title, firstImage, imgurl, imageUrl, overview, null, null);
        }

        // FoodEventDetail 조회
        FoodEventDetail foodDetail = foodEventDetailRepository.findByContentid(contentId);
        if (foodDetail != null) {
            FoodEvent foodEvent = foodEventRepository.findFirstByContentid(contentId).orElse(null);
            title = foodDetail.getTitle();
            overview = foodDetail.getOverview();
            firstImage = (foodEvent != null) ? foodEvent.getFirstimage() : null;
            contenttypeid = "39";
            return new EventDTO(contentId, contenttypeid, title, firstImage, imgurl, imageUrl, overview, null, null);
        }

        // LeisureSportsEventDetail 조회
        LeisureSportsEventDetail leisureDetail = leisureSportsEventDetailRepository.findByContentid(contentId);
        if (leisureDetail != null) {
            LeisureSportsEvent leisureEvent = leisureSportsEventRepository.findFirstByContentid(contentId).orElse(null);
            title = leisureDetail.getTitle();
            overview = leisureDetail.getOverview();
            firstImage = (leisureEvent != null) ? leisureEvent.getFirstimage() : null;
            contenttypeid = "28";
            return new EventDTO(contentId, contenttypeid, title, firstImage, imgurl, imageUrl, overview, null, null);
        }

        // LocalEventDetail 조회
        LocalEventDetail localEvent = localEventDetailRepository.findByContentid(contentId);
        if (localEvent != null) {
            LocalEvent local = localEventRepository.findFirstByContentid(contentId).orElse(null);
            title = localEvent.getTitle();
            overview = localEvent.getOverview();
            firstImage = (local != null) ? local.getFirstimage() : null;
            contenttypeid = "32";
            return new EventDTO(contentId, contenttypeid, title, firstImage, imgurl, imageUrl, overview, null, null);
        }

        // ShoppingEventDetail 조회
        ShoppingEventDetail shoppingEvent = shoppingEventDetailRepository.findByContentid(contentId);
        if (shoppingEvent != null) {
            ShoppingEvent shopping = shoppingEventRepository.findFirstByContentid(contentId).orElse(null);
            title = shoppingEvent.getTitle();
            overview = shoppingEvent.getOverview();
            firstImage = (shopping != null) ? shopping.getFirstimage() : null;
            contenttypeid = "38";
            return new EventDTO(contentId, contenttypeid, title, firstImage, imgurl, imageUrl, overview, null, null);
        }

        // TourEventDetail 조회
        TourEventDetail tourEvent = tourEventDetailRepository.findByContentid(contentId);
        if (tourEvent != null) {
            TourEvent tour = tourEventRepository.findFirstByContentid(contentId).orElse(null);
            title = tourEvent.getTitle();
            overview = tourEvent.getOverview();
            firstImage = (tour != null) ? tour.getFirstimage() : null;
            contenttypeid = "15";
            return new EventDTO(contentId, contenttypeid, title, firstImage, imgurl, imageUrl, overview, null, null);
        }

        // TouristAttractionDetail 조회
        TouristAttractionDetail touristAttraction = touristAttractionDetailRepository.findByContentid(contentId);
        if (touristAttraction != null) {
            TouristAttraction attraction = touristAttractionRepository.findFirstByContentid(contentId).orElse(null);
            title = touristAttraction.getTitle();
            overview = touristAttraction.getOverview();
            firstImage = (attraction != null) ? attraction.getFirstimage() : null;
            contenttypeid = "12";
            return new EventDTO(contentId, contenttypeid, title, firstImage, imgurl, imageUrl, overview, null, null);
        }

        // TravelCourse 조회
        TravelCourseDetail travelCourse = travelCourseDetailRepository.findByContentid(contentId);
        if (travelCourse != null) {
            TravelCourse course = travelCourseRepository.findFirstByContentid(contentId).orElse(null);
            title = travelCourse.getTitle();
            overview = travelCourse.getOverview();
            firstImage = (course != null) ? course.getImageUrl() : null; // 이미지가 imageurl
            contenttypeid = "25";
            return new EventDTO(contentId, contenttypeid, title, firstImage, imgurl, imageUrl, overview, null, null);
        }

        // 서울 이벤트 조회
        SeoulEvent seoulEvent = seoulEventRepository.findBySvcid(contentId);
        if (seoulEvent != null) {
            title = seoulEvent.getSvcnm();
            overview = seoulEvent.getDtlcont();
            imgurl = seoulEvent.getImgurl(); // 서울 이미지
            contenttypeid = "SeoulEventDetail";
            return new EventDTO(contentId, contenttypeid, title, null, imgurl, null, overview, seoulEvent.getSvcid(), null);
        }

        // 경기 이벤트 조회
        GyeonggiEvent gyeonggiEvent = gyeonggiEventRepository.findById(Long.parseLong(contentId)).orElse(null);
        if (gyeonggiEvent != null) {
            title = gyeonggiEvent.getTitle();
            imageUrl = gyeonggiEvent.getImageUrl(); // 경기 이미지
            contenttypeid = "GyeonggiEventDetail";
            return new EventDTO(contentId, contenttypeid, title, null, null, imageUrl, null, null, gyeonggiEvent.getId());
        }

        return null; // 해당 이벤트를 찾지 못한 경우
    }


}





