package side.side.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import side.side.model.*;
import side.side.repository.LocalEventDetailRepository;
import side.side.repository.LocalEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class LocalEventService {

    private static final Logger logger = Logger.getLogger(LocalEventService.class.getName());

    @Autowired
    private LocalEventRepository localEventRepository;

    @Autowired
    private LocalEventDetailRepository localEventDetailRepository;


    private final String serviceKey;

    public LocalEventService(){
        Dotenv dotenv = Dotenv.load();
        this.serviceKey = dotenv.get("CATEGORY_API_KEY");
    }
    // 지역 기반 관광 정보 API - 로컬 데이터 저장 / LocalEvent
    @Transactional(propagation = Propagation.REQUIRED)
    public List<LocalEvent> fetchAndSaveEventsLocal(String numOfRows, String pageNo) {
        numOfRows = "10";  // 호출되는 데이터의 개수를 10개로 제한
        List<LocalEvent> allLocalEvents = new ArrayList<>();
        boolean moreData = true;
        RestTemplate restTemplate = new RestTemplate();

        // 단일 요청
        String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/areaBasedList1")
                .queryParam("serviceKey", serviceKey)
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", pageNo)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "AppTest")
                .queryParam("arrange", "A")  // 제목순 정렬
                .queryParam("contentTypeId", "32")  // 숙박 contentTypeId
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
                    for (JsonNode node : itemsNode) {
                        LocalEvent event = new LocalEvent();
                        event.setAddr1(node.path("addr1").asText());
                        event.setBeginDe(node.path("begin_de").asText());
                        event.setCat1(node.path("cat1").asText());
                        event.setCat2(node.path("cat2").asText());
                        event.setCat3(node.path("cat3").asText());
                        event.setContentid(node.path("contentid").asText());
                        event.setContenttypeid(node.path("contenttypeid").asText());
                        event.setEndDe(node.path("end_de").asText());
                        event.setEventenddate(node.path("eventenddate").asText());
                        event.setEventstartdate(node.path("eventstartdate").asText());
                        event.setFirstimage(node.path("firstimage").asText());
                        event.setImageUrl(node.path("image_url").asText());
                        event.setRegionNm(node.path("region_nm").asText());
                        event.setTitle(node.path("title").asText());
                        event.setMapx(node.path("mapx").asText());
                        event.setMapy(node.path("mapy").asText());

                        Optional<LocalEvent> existingDetail = localEventRepository.findByContentidForUpdate(event.getContentid());
                        if (existingDetail.isPresent()) {
                            continue;
                        }

                        localEventRepository.upsertLocalEvent(
                                event.getContentid(),
                                event.getTitle(),
                                event.getAddr1(),
                                event.getFirstimage(),
                                event.getBeginDe(),
                                event.getEndDe(),
                                event.getCat1(),
                                event.getCat2(),
                                event.getCat3(),
                                event.getEventstartdate(),
                                event.getEventenddate(),
                                event.getImageUrl(),
                                event.getRegionNm(),
                                event.getContenttypeid(),
                                event.getMapx(),
                                event.getMapy()
                        );

                        allLocalEvents.add(event);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allLocalEvents;
    }

        //모든 데이터 받아올때
//        while (moreData) {
//            String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/areaBasedList1")
//                    .queryParam("serviceKey", serviceKey)
//                    .queryParam("numOfRows", numOfRows)
//                    .queryParam("pageNo", pageNo)
//                    .queryParam("MobileOS", "ETC")
//                    .queryParam("MobileApp", "AppTest")
//                    .queryParam("arrange", "A")  // 기본 정렬을 제목순으로 설정
//                    .queryParam("contentTypeId", "32")
//                    .queryParam("_type", "json")
//                    .build()
//                    .toUriString();
//
//            logger.info("Request URL: " + url);  // Request URL을 로깅
//
//            try {
//                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
//                logger.info("Response Status: " + response.getStatusCode());
//                logger.info("Response Body: " + response.getBody());  // Response Body를 로깅
//
//                if (response.getStatusCode().is2xxSuccessful()) {
//                    try {
//                        ObjectMapper objectMapper = new ObjectMapper();
//                        JsonNode rootNode = objectMapper.readTree(response.getBody());
//                        JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");
//
//                        if (itemsNode.isArray()) {
//                            List<LocalEvent> events = new ArrayList<>();
//                            for (JsonNode node : itemsNode) {
//                                LocalEvent event = new LocalEvent();
//                                event.setTitle(node.path("title").asText());
//                                event.setAddr1(node.path("addr1").asText());
//                                event.setEventstartdate(node.path("eventstartdate").asText());
//                                event.setEventenddate(node.path("eventenddate").asText());
//                                event.setFirstimage(node.path("firstimage").asText());
//                                event.setCat1(node.path("cat1").asText());
//                                event.setCat2(node.path("cat2").asText());
//                                event.setCat3(node.path("cat3").asText());
//                                event.setContentid(node.path("contentid").asText());
//                                event.setContenttypeid(node.path("contenttypeid").asText());
//                                events.add(event);
//                            }
//                            localEventRepository.saveAll(events);  // LocalEvent로 저장
//                            allEvents.addAll(events);
//
//                            if (itemsNode.size() < Integer.parseInt(numOfRows)) {
//                                moreData = false;
//                            } else {
//                                pageNo = String.valueOf(Integer.parseInt(pageNo) + 1);
//                            }
//                        } else {
//                            moreData = false;
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
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

    // 지역 기반 관광 정보 API - 로컬 이벤트 상세 정보 저장
    @Transactional(propagation = Propagation.REQUIRED)
    public void fetchAndSaveLocalEventDetail(String contentid) {
        RestTemplate restTemplate = new RestTemplate();

        String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/detailCommon1")
                .queryParam("serviceKey", serviceKey)
                .queryParam("contentId", contentid)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "AppTest")
                .queryParam("defaultYN", "Y")
                .queryParam("mapinfoYN", "Y")
                .queryParam("addrinfoYN", "Y")
                .queryParam("overviewYN", "Y")
                .queryParam("_type", "json")
                .build()
                .toUriString();

        logger.info("로컬 이벤트 상세 정보 가져오기: contentId = " + contentid);
        logger.info("요청 URL: " + url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("API 응답: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode itemsNode = rootNode.path("response").path("body").path("items");

                if (itemsNode.isMissingNode() || !itemsNode.has("item")) {
                    logger.warning("해당 contentId에 대한 로컬 이벤트 상세 정보가 없습니다: " + contentid);
                    return;
                }

                JsonNode itemNode = itemsNode.path("item").get(0);

                if (itemNode == null) {
                    logger.warning("해당 contentId에 대한 항목이 없습니다: " + contentid);
                    return;
                }

                LocalEventDetail eventDetail = new LocalEventDetail();
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

                // 데이터에 락 걸기
                Optional<LocalEventDetail> existingDetail = localEventDetailRepository.findByContentidForUpdate(contentid);
                if (existingDetail.isPresent()) {
                    return;
                }

                // Upsert 사용하여 데이터 삽입 또는 업데이트
                localEventDetailRepository.upsertLocalEventDetail(
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
                        eventDetail.getCpyrhtDivCd(),
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
    // 소개 정보 API 호출 메소드
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

        logger.info("숙박 시설 소개 정보 가져오기: contentId = " + contentid);
        logger.info("요청 URL: " + url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("API 응답: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readTree(response.getBody()).path("response").path("body").path("items").path("item").get(0);
            } else {
                logger.warning("해당 contentId에 대한 숙박 시설 소개 정보를 가져오지 못했습니다: " + contentid);
            }
        } catch (Exception e) {
            logger.severe("contentId: " + contentid + "에 대한 숙박 시설 소개 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // 이미지 정보 API 호출 메소드
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


        logger.info("숙박 시설 이미지 정보 가져오기: contentId = " + contentid);
        logger.info("요청 URL: " + url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("API 응답: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readTree(response.getBody()).path("response").path("body").path("items").path("item");
            } else {
                logger.warning("해당 contentId에 대한 숙박 시설 이미지 정보를 가져오지 못했습니다: " + contentid);
            }
        } catch (Exception e) {
            logger.severe("contentId: " + contentid + "에 대한 숙박 시설 이미지 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
    // 데이터베이스에서 숙박 시설 상세 정보 추출
    @Transactional
    public LocalEventDetail getAccommodationDetailFromDB(String contentid) {
        return localEventDetailRepository.findByContentid(contentid);
    }

    // 카테고리 기반 숙박 시설 리스트 반환
    @Transactional
    public List<LocalEvent> getAccommodationsByCategory(String category) {
        // 카테고리 맵핑 로직에 따라 contentTypeId를 설정
        String contentTypeId = "32"; // 32 숙박 시설 설정

        return localEventRepository.findByContenttypeid(contentTypeId); // 필요에 따라 로직 변경
    }
    // '서울특별시'에 해당하는 숙박 이벤트 가져오기
    @Transactional
    public List<LocalEvent> getLocalEventsByRegion(String region) {
        return localEventRepository.findAll().stream()
                .filter(event -> event.getAddr1().contains(region))
                .collect(Collectors.toList());
    }
    // 유사한 여행지 정보 가져오기
    @Transactional
    public List<LocalEvent> getSimilarLocalEvents(String contenttypeid) {
        return localEventRepository.findByContenttypeid(contenttypeid);
    }

    // contentid로 LocalEvent 조회
    @Transactional
    public List<LocalEvent> findBycontentid(String contentid) {
        return localEventRepository.findBycontentid(contentid);
    }
}
