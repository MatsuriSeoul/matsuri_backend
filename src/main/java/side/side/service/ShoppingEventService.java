package side.side.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import side.side.model.*;
import side.side.repository.ShoppingEventDetailRepository;
import side.side.repository.ShoppingEventRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class ShoppingEventService {

    private static final Logger logger = Logger.getLogger(ShoppingEventService.class.getName());

    @Autowired
    private ShoppingEventRepository shoppingEventRepository;

    @Autowired
    private ShoppingEventDetailRepository shoppingEventDetailRepository;
    private final String serviceKey;

    public ShoppingEventService(){
        Dotenv dotenv = Dotenv.load();
        this.serviceKey = dotenv.get("CATEGORY_API_KEY");
    }
    private final String baseUrl = "http://apis.data.go.kr/B551011/KorService1/areaBasedList1";

    // 쇼핑 API 호출 및 데이터 저장
    @Transactional(propagation = Propagation.REQUIRED)
    public List<ShoppingEvent> fetchAndSaveShoppingEvents(String numOfRows, String pageNo) {
        List<ShoppingEvent> allShoppingEvents = new ArrayList<>();
        boolean moreData = true;
        numOfRows = "10";  // 호출되는 데이터의 개수를 10개로 제한
        RestTemplate restTemplate = new RestTemplate();

        // 단일 요청
        String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/areaBasedList1")
                .queryParam("serviceKey", serviceKey)
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", pageNo)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "AppTest")
                .queryParam("arrange", "A")  // 제목순 정렬
                .queryParam("contentTypeId", "38")  // 쇼핑의 contentTypeId
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
                        ShoppingEvent event = new ShoppingEvent();
                        event.setTitle(node.path("title").asText());
                        event.setAddr1(node.path("addr1").asText());
                        event.setFirstimage(node.path("firstimage").asText());
                        event.setMapx(node.path("mapx").asText());
                        event.setMapy(node.path("mapy").asText());
                        event.setContentid(node.path("contentid").asText());
                        event.setContenttypeid(node.path("contenttypeid").asText());
                        event.setAreacode(node.path("areacode").asText());
                        event.setSigungucode(node.path("sigungucode").asText());
                        event.setTel(node.path("tel").asText());
                        event.setOverview(node.path("overview").asText());


                        Optional<ShoppingEvent> existingDetail = shoppingEventRepository.findByContentidForUpdate(event.getContentid());
                        if (existingDetail.isPresent()) {
                            continue;
                        }

                        shoppingEventRepository.upsertShoppingEvent(
                                event.getContentid(),
                                event.getTitle(),
                                event.getAddr1(),
                                event.getFirstimage(),
                                event.getMapx(),
                                event.getMapy(),
                                event.getContenttypeid()
                        );

                        allShoppingEvents.add(event);
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return allShoppingEvents;
    }
    //모든 데이터 불러오기
//        while (moreData) {
//            String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/areaBasedList1")
//                    .queryParam("serviceKey", serviceKey)
//                    .queryParam("numOfRows", numOfRows)
//                    .queryParam("pageNo", pageNo)
//                    .queryParam("MobileOS", "ETC")
//                    .queryParam("MobileApp", "AppTest")
//                    .queryParam("arrange", "A")  // 제목순 정렬
//                    .queryParam("contentTypeId", "38")  // 쇼핑의 contentTypeId
//                    .queryParam("_type", "json")
//                    .build()
//                    .toUriString();
//
//            logger.info("Request URL: " + url);
//
//            try {
//                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
//                logger.info("Response Status: " + response.getStatusCode());
//                logger.info("Response Body: " + response.getBody());
//
//                if (response.getStatusCode().is2xxSuccessful()) {
//                    try {
//                        ObjectMapper objectMapper = new ObjectMapper();
//                        JsonNode rootNode = objectMapper.readTree(response.getBody());
//                        JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");
//
//                        if (itemsNode.isArray()) {
//                            List<ShoppingEvent> shoppingEvents = new ArrayList<>();
//                            for (JsonNode node : itemsNode) {
//                                ShoppingEvent event = new ShoppingEvent();
//                                event.setTitle(node.path("title").asText());
//                                event.setAddr1(node.path("addr1").asText());
//                                event.setFirstimage(node.path("firstimage").asText());
//                                event.setMapx(node.path("mapx").asText());
//                                event.setMapy(node.path("mapy").asText());
//                                event.setContentid(node.path("contentid").asText());
//                                event.setContenttypeid(node.path("contenttypeid").asText());
//                                event.setAreacode(node.path("areacode").asText());
//                                event.setSigungucode(node.path("sigungucode").asText());
//                                event.setTel(node.path("tel").asText());
//                                event.setOverview(node.path("overview").asText());
//                                shoppingEvents.add(event);
//                            }
//                            shoppingEventRepository.saveAll(shoppingEvents);
//                            allShoppingEvents.addAll(shoppingEvents);
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
//        return allShoppingEvents;
//    }
    // 쇼핑 이벤트 상세 정보 저장
    @Transactional(propagation = Propagation.REQUIRED)
    public void fetchAndSaveShoppingEventDetail(String contentid) {
        RestTemplate restTemplate = new RestTemplate();

        String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/detailCommon1")
                .queryParam("serviceKey", serviceKey)
                .queryParam("contentId", contentid)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "AppTest")
                .queryParam("defaultYN", "Y")
                .queryParam("addrinfoYN", "Y")
                .queryParam("overviewYN", "Y")
                .queryParam("mapinfoYN", "Y")
                .queryParam("_type", "json")
                .build()
                .toUriString();

        logger.info("쇼핑 이벤트 상세 정보 가져오기: contentId = " + contentid);
        logger.info("요청 URL: " + url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode itemNode = rootNode.path("response").path("body").path("items").path("item").get(0);

                if (itemNode.isMissingNode()) {
                    return;
                }

                ShoppingEventDetail eventDetail = new ShoppingEventDetail();
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
                Optional<ShoppingEventDetail> existingDetail = shoppingEventDetailRepository.findByContentidForUpdate(contentid);
                if (existingDetail.isPresent()) {
                    return;
                }

                // Upsert 사용하여 데이터 삽입 또는 업데이트
                shoppingEventDetailRepository.upsertShoppingEventDetail(
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

        logger.info("쇼핑 소개 정보 가져오기: contentId = " + contentid);
        logger.info("요청 URL: " + url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("API 응답: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readTree(response.getBody()).path("response").path("body").path("items").path("item").get(0);
            } else {
                logger.warning("해당 contentId에 대한 쇼핑 소개 정보를 가져오지 못했습니다: " + contentid);
            }
        } catch (Exception e) {
            logger.severe("contentId: " + contentid + "에 대한 쇼핑 소개 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
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

        logger.info("쇼핑 이미지 정보 가져오기: contentId = " + contentid);
        logger.info("요청 URL: " + url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("API 응답: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readTree(response.getBody()).path("response").path("body").path("items").path("item");
            } else {
                logger.warning("해당 contentId에 대한 쇼핑 이미지 정보를 가져오지 못했습니다: " + contentid);
            }
        } catch (Exception e) {
            logger.severe("contentId: " + contentid + "에 대한 쇼핑 이미지 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
    // 데이터베이스에서 숙박 시설 상세 정보 추출
    @Transactional
    public ShoppingEventDetail getShoppingEventDetailFromDB(String contentid) {
        return shoppingEventDetailRepository.findByContentid(contentid);
    }

    // 카테고리 기반 숙박 시설 리스트 반환
    @Transactional
    public List<ShoppingEvent> getShoppingEventsByCategory(String category) {
        // 카테고리 맵핑 로직에 따라 contentTypeId를 설정
        String contentTypeId = "38"; // 38 쇼핑  설정

        return shoppingEventRepository.findByContenttypeid(contentTypeId); // 필요에 따라 로직 변경
    }
    // '서울특별시'에 해당하는 쇼핑 이벤트 가져오기
    @Transactional
    public List<ShoppingEvent> getShoppingEventsByRegion(String region) {
        return shoppingEventRepository.findAll().stream()
                .filter(event -> event.getAddr1().contains(region))
                .collect(Collectors.toList());
    }
    // 유사한 여행지 정보 가져오기
    @Transactional
    public List<ShoppingEvent> getSimilarShoppingEvents(String contenttypeid) {
        return shoppingEventRepository.findByContenttypeid(contenttypeid);
    }

    // contentid로 ShoppingEvent 조회
    @Transactional
    public List<ShoppingEvent> findBycontentid(String contentid) {
        return shoppingEventRepository.findBycontentid(contentid);
    }

}


