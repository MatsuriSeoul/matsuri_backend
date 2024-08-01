package side.side.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import side.side.model.*;
import side.side.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
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
    private TourEventRepository tourEventRepository;

    @Autowired
    private TourEventDetailRepository tourEventDetailRepository;

    @Autowired
    private LocalEventRepository localEventRepository;

    @Autowired
    private TouristAttractionRepository touristAttractionRepository;

    @Autowired
    private LeisureSportsEventRepository leisureSportsEventRepository;

    // 키 값 절대 건들이면 안됨
    private final String gyeonggiApiKey = "77b3011d245e4ca68e85caec7fd610ae";
    private final String seoulApiKey = "754578757270626739386969624e71";
    private final String serviceKey = "13jkaARutXp/OwAHynRnYjP7BJuMVGIZx2Ki3dRMaDlcBqrfZHC9Zk97LCCuLyKfiR2cVhyWy59t96rPwyWioA==";

    // 경기도 행사 API
    public void fetchAndSaveGyeonggiEvents() {
        int pageSize = 100;
        int startIndex = 1;
        boolean moreData = true;
        RestTemplate restTemplate = new RestTemplate();

        while (moreData) {
            String url = String.format("https://openapi.gg.go.kr/GGCULTUREVENTSTUS?KEY=%s&Type=json&pIndex=%d&pSize=%d",
                    gyeonggiApiKey, startIndex, pageSize);
            try {
                String response = restTemplate.getForObject(url, String.class);
                System.out.println("경기도 API 응답" + response);

                if (response.startsWith("<")) {
                    throw new IllegalArgumentException("존재하지 않는 응답 API");
                }

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode dataNode = rootNode.path("GGCULTUREVENTSTUS").path(1).path("row");

                if (dataNode.isArray()) {
                    List<GyeonggiEvent> events = new ArrayList<>();
                    for (JsonNode node : dataNode) {
                        GyeonggiEvent event = new GyeonggiEvent();
                        event.setInstNm(node.path("INST_NM").asText());
                        event.setTitle(node.path("TITLE").asText());
                        event.setCategoryNm(node.path("CATEGORY_NM").asText());
                        event.setUrl(node.path("URL").asText());
                        event.setImageUrl(node.path("IMAGE_URL").asText());
                        event.setBeginDe(node.path("BEGIN_DE").asText());
                        event.setEndDe(node.path("END_DE").asText());
                        event.setAddr(node.path("ADDR").asText());
                        event.setEventTmInfo(node.path("EVENT_TM_INFO").asText());
                        event.setPartcptExpnInfo(node.path("PARTCPT_EXPN_INFO").asText());
                        event.setTelnoInfo(node.path("TELNO_INFO").asText());
                        event.setHostInstNm(node.path("HOST_INST_NM").asText());
                        event.setHmpgUrl(node.path("HMPG_URL").asText());
                        event.setWritngDe(node.path("WRITNG_DE").asText());
                        events.add(event);
                    }
                    gyeonggiEventRepository.saveAll(events);

                    if (dataNode.size() < pageSize) {
                        moreData = false;
                    } else {
                        startIndex++;
                    }
                } else {
                    moreData = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                moreData = false;
            }
        }
    }

    // 서울 행사 API
    public void fetchAndSaveSeoulEvents() {
        int pageSize = 100;
        int startIndex = 1;
        boolean moreData = true;
        RestTemplate restTemplate = new RestTemplate();

        while (moreData) {
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
                    }
                    seoulEventRepository.saveAll(events);

                    if (dataNode.size() < pageSize) {
                        moreData = false;
                    } else {
                        startIndex += pageSize;
                    }
                } else {
                    moreData = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                moreData = false;
            }
        }
    }

    // 한국관광공사_국문 관광정보 서비스_GW API
        public List<TourEvent> fetchAndSaveEvents(String serviceKey, String numOfRows, String pageNo, String eventStartDate) {
        List<TourEvent> allEvents = new ArrayList<>();
        boolean moreData = true;
        RestTemplate restTemplate = new RestTemplate();

        while (moreData) {
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
                    // JSON 응답 처리
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode rootNode = objectMapper.readTree(response.getBody());
                        JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");

                        if (itemsNode.isArray()) {
                            List<TourEvent> events = new ArrayList<>();
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
                                event.setContentid(node.path("contentid").asText());
                                event.setContenttypeid(node.path("contenttypeid").asText());
                                events.add(event);
                            }
                            tourEventRepository.saveAll(events);
                            allEvents.addAll(events);

                            // 만약 현재 페이지의 이벤트 수가 요청한 numOfRows보다 적다면 더 이상 데이터가 없다고 판단
                            if (itemsNode.size() < Integer.parseInt(numOfRows)) {
                                moreData = false;
                            } else {
                                // 다음 페이지로 이동
                                pageNo = String.valueOf(Integer.parseInt(pageNo) + 1);
                            }
                        } else {
                            moreData = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        moreData = false;
                    }
                } else {
                    moreData = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                moreData = false;
            }
        }

        return allEvents;
    }

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
                .queryParam("_type", "json")
                .build()
                .toUriString();

     //  logger.info("Fetching event detail for contentId: " + contentid);
       // logger.info("Request URL: " + url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
          //  logger.info("API Response: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode itemNode = rootNode.path("response").path("body").path("items").path("item").get(0);

                if (itemNode.isMissingNode()) {
                    logger.warning("No event details found for contentId: " + contentid);
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
                eventDetail.setCpyrhtDivCd(itemNode.path("cpyrhtDivCd").asText());
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

                tourEventDetailRepository.save(eventDetail);
              //  logger.info("Event detail saved for contentId: " + contentid);
            } else {
                logger.warning("Failed to fetch event detail for contentId: " + contentid);
            }
        } catch (Exception e) {
           // logger.severe("Error fetching event detail for contentId: " + contentid + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    // 지역 기반 관광 정보 API - 로컬 데이터 저장
    public List<LocalEvent> fetchAndSaveEventsLocal(String numOfRows, String pageNo) {
        List<LocalEvent> allEvents = new ArrayList<>();
        boolean moreData = true;
        RestTemplate restTemplate = new RestTemplate();

        while (moreData) {
            String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/areaBasedList1")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("numOfRows", numOfRows)
                    .queryParam("pageNo", pageNo)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "AppTest")
                    .queryParam("arrange", "A")  // 기본 정렬을 제목순으로 설정
                    .queryParam("contentTypeId", "32")
                    .queryParam("_type", "json")
                    .build()
                    .toUriString();

            logger.info("Request URL: " + url);  // Request URL을 로깅

            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                logger.info("Response Status: " + response.getStatusCode());
                logger.info("Response Body: " + response.getBody());  // Response Body를 로깅

                if (response.getStatusCode().is2xxSuccessful()) {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode rootNode = objectMapper.readTree(response.getBody());
                        JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");

                        if (itemsNode.isArray()) {
                            List<LocalEvent> events = new ArrayList<>();
                            for (JsonNode node : itemsNode) {
                                LocalEvent event = new LocalEvent();
                                event.setTitle(node.path("title").asText());
                                event.setAddr1(node.path("addr1").asText());
                                event.setEventstartdate(node.path("eventstartdate").asText());
                                event.setEventenddate(node.path("eventenddate").asText());
                                event.setFirstimage(node.path("firstimage").asText());
                                event.setCat1(node.path("cat1").asText());
                                event.setCat2(node.path("cat2").asText());
                                event.setCat3(node.path("cat3").asText());
                                event.setContentid(node.path("contentid").asText());
                                event.setContenttypeid(node.path("contenttypeid").asText());
                                events.add(event);
                            }
                            localEventRepository.saveAll(events);  // LocalEvent로 저장
                            allEvents.addAll(events);

                            if (itemsNode.size() < Integer.parseInt(numOfRows)) {
                                moreData = false;
                            } else {
                                pageNo = String.valueOf(Integer.parseInt(pageNo) + 1);
                            }
                        } else {
                            moreData = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        moreData = false;
                    }
                } else {
                    moreData = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                moreData = false;
            }
        }

        return allEvents;
    }

    // contentTypeId 12인 관광지 db 저장하기
    public List<TouristAttraction> fetchAndSaveTouristAttractions(String numOfRows, String pageNo) {
        List<TouristAttraction> allAttractions = new ArrayList<>();
        boolean moreData = true;
        RestTemplate restTemplate = new RestTemplate();

        while (moreData) {
            String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/areaBasedList1")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("numOfRows", numOfRows)
                    .queryParam("pageNo", pageNo)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "AppTest")
                    .queryParam("arrange", "A")
                    .queryParam("contentTypeId", "12")
                    .queryParam("_type", "json")
                    .build()
                    .toUriString();

            logger.info("Request URL: " + url);

            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                logger.info("Response Status: " + response.getStatusCode());
                logger.info("Response Body: " + response.getBody());

                if (response.getStatusCode().is2xxSuccessful()) {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode rootNode = objectMapper.readTree(response.getBody());
                        JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");

                        if (itemsNode.isArray()) {
                            List<TouristAttraction> attractions = new ArrayList<>();
                            for (JsonNode node : itemsNode) {
                                TouristAttraction attraction = new TouristAttraction();
                                attraction.setTitle(node.path("title").asText());
                                attraction.setAddr1(node.path("addr1").asText());
                                attraction.setFirstimage(node.path("firstimage").asText());
                                attraction.setMapx(node.path("mapx").asText());
                                attraction.setMapy(node.path("mapy").asText());
                                attraction.setOverview(node.path("overview").asText());
                                attraction.setContentid(node.path("contentid").asText());
                                attraction.setContenttypeid(node.path("contenttypeid").asText());
                                attraction.setAreacode(node.path("areacode").asText());
                                attraction.setSigungucode(node.path("sigungucode").asText());
                                attraction.setZipcode(node.path("zipcode").asText());
                                attraction.setTel(node.path("tel").asText());
                                attraction.setModifiedtime(node.path("modifiedtime").asText());
                                attractions.add(attraction);
                            }
                            touristAttractionRepository.saveAll(attractions);
                            allAttractions.addAll(attractions);

                            if (itemsNode.size() < Integer.parseInt(numOfRows)) {
                                moreData = false;
                            } else {
                                pageNo = String.valueOf(Integer.parseInt(pageNo) + 1);
                            }
                        } else {
                            moreData = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        moreData = false;
                    }
                } else {
                    moreData = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                moreData = false;
            }
        }

        return allAttractions;
    }
    // contentTypeId가 12인 관광지의 상세 정보 디비 저장
    public void fetchAndSaveAttractionDetail(String contentid) {
        RestTemplate restTemplate = new RestTemplate();

        String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/detailCommon1")
                .queryParam("serviceKey", serviceKey)
                .queryParam("contentId", contentid)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "AppTest")
                .queryParam("defaultYN", "Y")
                .queryParam("addrinfoYN", "Y")
                .queryParam("overviewYN", "Y")
                .queryParam("_type", "json")
                .build()
                .toUriString();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode itemNode = rootNode.path("response").path("body").path("items").path("item").get(0);

                if (!itemNode.isMissingNode()) {
                    TouristAttraction attraction = new TouristAttraction();
                    attraction.setContentid(itemNode.path("contentid").asText());
                    attraction.setTitle(itemNode.path("title").asText());
                    attraction.setAddr1(itemNode.path("addr1").asText());
                    attraction.setOverview(itemNode.path("overview").asText());
                    attraction.setFirstimage(itemNode.path("firstimage").asText());
                    attraction.setMapx(itemNode.path("mapx").asText());
                    attraction.setMapy(itemNode.path("mapy").asText());
                    attraction.setTel(itemNode.path("tel").asText());
                    attraction.setAreacode(itemNode.path("areacode").asText());
                    attraction.setSigungucode(itemNode.path("sigungucode").asText());
                    attraction.setZipcode(itemNode.path("zipcode").asText());
                    attraction.setModifiedtime(itemNode.path("modifiedtime").asText());

                    touristAttractionRepository.save(attraction);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //ContentypeId 28인 레포츠 불러와 저장하기
    public List<LeisureSportsEvent> fetchAndSaveLeisureSportsEvents(String numOfRows, String pageNo) {
        List<LeisureSportsEvent> allEvents = new ArrayList<>();
        boolean moreData = true;
        RestTemplate restTemplate = new RestTemplate();

        while (moreData) {
            String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/areaBasedList1")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("numOfRows", numOfRows)
                    .queryParam("pageNo", pageNo)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "AppTest")
                    .queryParam("contentTypeId", "28")
                    .queryParam("_type", "json")
                    .build()
                    .toUriString();

            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(response.getBody());
                    JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");

                    if (itemsNode.isArray()) {
                        List<LeisureSportsEvent> events = new ArrayList<>();
                        for (JsonNode node : itemsNode) {
                            LeisureSportsEvent event = new LeisureSportsEvent();
                            event.setTitle(node.path("title").asText());
                            event.setAddr1(node.path("addr1").asText());
                            event.setEventstartdate(node.path("eventstartdate").asText());
                            event.setEventenddate(node.path("eventenddate").asText());
                            event.setFirstimage(node.path("firstimage").asText());
                            event.setCat1(node.path("cat1").asText());
                            event.setCat2(node.path("cat2").asText());
                            event.setCat3(node.path("cat3").asText());
                            event.setContentid(node.path("contentid").asText());
                            event.setContenttypeid(node.path("contenttypeid").asText());
                            events.add(event);
                        }
                        leisureSportsEventRepository.saveAll(events);
                        allEvents.addAll(events);

                        if (itemsNode.size() < Integer.parseInt(numOfRows)) {
                            moreData = false;
                        } else {
                            pageNo = String.valueOf(Integer.parseInt(pageNo) + 1);
                        }
                    } else {
                        moreData = false;
                    }
                } else {
                    moreData = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                moreData = false;
            }
        }

        return allEvents;
    }

    public void fetchAndSaveAllEventDetails() {
        List<String> contentIds = getAllContentIds();
        for (String contentId : contentIds) {
            fetchAndSaveEventDetail(contentId);
        }
    }

    public List<String> getAllContentIds() {
        return tourEventRepository.findAll().stream()
                .map(event -> event.getContentid())
                .collect(Collectors.toList());
    }



    public List<Object> searchEvents(String date, String region, String category) {
        List<Object> results = new ArrayList<>();

        if (region == null || region.equalsIgnoreCase("경기")) {
            results.addAll(gyeonggiEventRepository.findByCriteria(date, category));
        }

        if (region == null || region.equalsIgnoreCase("서울")) {
            results.addAll(seoulEventRepository.findByCriteria(date, category));
        }

        return results;
    }

}




