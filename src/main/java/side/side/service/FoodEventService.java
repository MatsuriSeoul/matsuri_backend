package side.side.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import side.side.model.*;
import side.side.repository.FoodEventDetailRepository;
import side.side.repository.FoodEventRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class FoodEventService {

    private static final Logger logger = Logger.getLogger(FoodEventService.class.getName());

    @Autowired
    private FoodEventRepository foodEventRepository;

    @Autowired
    private FoodEventDetailRepository foodEventDetailRepository;

    private final String serviceKey = "13jkaARutXp/OwAHynRnYjP7BJuMVGIZx2Ki3dRMaDlcBqrfZHC9Zk97LCCuLyKfiR2cVhyWy59t96rPwyWioA==";

    // 음식 API 호출 및 데이터 저장
    public List<FoodEvent> fetchAndSaveFoodEvents(String numOfRows, String pageNo) {
        numOfRows = "10";  // 호출되는 데이터의 개수를 10개로 제한
        List<FoodEvent> allFoodEvents = new ArrayList<>();
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
                .queryParam("contentTypeId", "39")  // 음식의 contentTypeId
                .queryParam("_type", "json")
                .build()
                .toUriString();

        logger.info("응답 URL: " + url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("응답 상태" + response.getStatusCode());
            logger.info("응답 본문" + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");

                if (itemsNode.isArray()) {
                    List<FoodEvent> foodEvents = new ArrayList<>();
                    for (JsonNode node : itemsNode) {
                        FoodEvent event = new FoodEvent();
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
                        foodEvents.add(event);
                    }
                    foodEventRepository.saveAll(foodEvents);
                    allFoodEvents.addAll(foodEvents);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //모든 데이터 받아올때
//        while (moreData) {
//            String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/areaBasedList1")
//                    .queryParam("serviceKey", serviceKey)
//                    .queryParam("numOfRows", numOfRows)
//                    .queryParam("pageNo", pageNo)
//                    .queryParam("MobileOS", "ETC")
//                    .queryParam("MobileApp", "AppTest")
//                    .queryParam("arrange", "A")  // 제목순 정렬
//                    .queryParam("contentTypeId", "39")  // 음식의 contentTypeId
//                    .queryParam("_type", "json")
//                    .build()
//                    .toUriString();
//
//            logger.info("응답 URL: " + url);
//
//            try {
//                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
//                logger.info("응답 상태" + response.getStatusCode());
//                logger.info("응답 본문" + response.getBody());
//
//                if (response.getStatusCode().is2xxSuccessful()) {
//                    try {
//                        ObjectMapper objectMapper = new ObjectMapper();
//                        JsonNode rootNode = objectMapper.readTree(response.getBody());
//                        JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");
//
//                        if (itemsNode.isArray()) {
//                            List<FoodEvent> foodEvents = new ArrayList<>();
//                            for (JsonNode node : itemsNode) {
//                                FoodEvent event = new FoodEvent();
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
//                                foodEvents.add(event);
//                            }
//                            foodEventRepository.saveAll(foodEvents);
//                            allFoodEvents.addAll(foodEvents);
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

        return allFoodEvents;
    }

    // FoodEventDetail 저장
    public void fetchAndSaveFoodEventDetail(String contentid) {
        RestTemplate restTemplate = new RestTemplate();

        String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/detailCommon1")
                .queryParam("serviceKey", serviceKey)
                .queryParam("contentId", contentid)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "AppTest")
                .queryParam("defaultYN", "Y")
                .queryParam("addrinfoYN", "Y")
                .queryParam("mapinfoYN", "Y")
                .queryParam("overviewYN", "Y")
                .queryParam("_type", "json")
                .build()
                .toUriString();

        logger.info("contentId에 대한 음식 이벤트 세부 정보를 가져오는 중:" + contentid);
        logger.info("요청 URL:" + url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("API 응답: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode itemsNode = rootNode.path("response").path("body").path("items");

                // items 노드가 비어있거나 item 노드가 없는 경우를 처리
                if (itemsNode.isMissingNode() || !itemsNode.has("item")) {
                    logger.warning("contentId에 대한 음식 이벤트 세부 정보를 찾을 수 없습니다:" + contentid);
                    return;
                }

                JsonNode itemNode = itemsNode.path("item").get(0); // 첫 번째 아이템만 사용

                if (itemNode == null) {
                    logger.warning("contentId에 대한 항목을 찾을 수 없습니다:" + contentid);
                    return;
                }

                FoodEventDetail eventDetail = new FoodEventDetail();
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

                foodEventDetailRepository.save(eventDetail);
                logger.info("contentId에 대한 음식 이벤트 세부 정보가 저장되었습니다:" + contentid);
            } else {
                logger.warning("contentId에 대한 음식 이벤트 세부 정보를 가져오지 못했습니다:" + contentid);
            }
        } catch (Exception e) {
            logger.severe("contentId에 대한 음식 이벤트 세부 정보를 가져오는 중 오류가 발생했습니다." + contentid + ": " + e.getMessage());
            e.printStackTrace();
        }
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

        logger.info("음식 이벤트 이미지 정보 가져오기: contentId = " + contentid);
        logger.info("요청 URL: " + url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("API 응답: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readTree(response.getBody()).path("response").path("body").path("items").path("item");
            } else {
                logger.warning("해당 contentId에 대한 음식 이벤트 이미지 정보를 가져오지 못했습니다: " + contentid);
            }
        } catch (Exception e) {
            logger.severe("contentId: " + contentid + "에 대한 음식 이벤트 이미지 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
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

        logger.info("음식 이벤트 소개 정보 가져오기: contentId = " + contentid);
        logger.info("요청 URL: " + url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("API 응답: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readTree(response.getBody()).path("response").path("body").path("items").path("item").get(0);
            } else {
                logger.warning("해당 contentId에 대한 음식 이벤트 소개 정보를 가져오지 못했습니다: " + contentid);
            }
        } catch (Exception e) {
            logger.severe("contentId: " + contentid + "에 대한 음식 이벤트 소개 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
    // 데이터베이스에서 숙박 시설 상세 정보 추출
    public FoodEventDetail getFoodEventDetailFromDB(String contentid) {
        return foodEventDetailRepository.findByContentid(contentid);
    }

    // 카테고리 기반 숙박 시설 리스트 반환
    public List<FoodEvent> getFoodEventsByCategory(String category) {
        // 카테고리 맵핑 로직에 따라 contentTypeId를 설정
        String contentTypeId = "39"; // 39 음식 설정

        return foodEventRepository.findByContenttypeid(contentTypeId); // 필요에 따라 로직 변경
    }
    // '서울특별시'에 해당하는 쇼핑 이벤트 가져오기
    public List<FoodEvent> getFoodEventsByRegion(String region) {
        return foodEventRepository.findAll().stream()
                .filter(event -> event.getAddr1().contains(region))
                .collect(Collectors.toList());
    }
    // 유사한 여행지 정보 가져오기
    public List<FoodEvent> getSimilarFoodEvents(String contenttypeid) {
        return foodEventRepository.findByContenttypeid(contenttypeid);
    }

    // contentid로 FoodEvent 조회
    public Optional<FoodEvent> findBycontentid(String contentid) {
        return foodEventRepository.findBycontentid(contentid);
    }

}
