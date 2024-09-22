package side.side.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import side.side.model.*;
import side.side.repository.LeisureSportsEventDetailRepository;
import side.side.repository.LeisureSportsEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class LeisureSportsEventService {

    private static final Logger logger = Logger.getLogger(LeisureSportsEventService.class.getName());

    @Autowired
    private LeisureSportsEventRepository leisureSportsEventRepository;

    @Autowired
    private LeisureSportsEventDetailRepository leisureSportsEventDetailRepository;

    private final String serviceKey = "13jkaARutXp/OwAHynRnYjP7BJuMVGIZx2Ki3dRMaDlcBqrfZHC9Zk97LCCuLyKfiR2cVhyWy59t96rPwyWioA==";

    // ContentypeId 28인 레포츠 불러와 저장하기
    public List<LeisureSportsEvent> fetchAndSaveLeisureSportsEvents(String numOfRows, String pageNo) {
        List<LeisureSportsEvent> leisureSportsEvents = new ArrayList<>();  // 리스트 초기화
        numOfRows = "500";  // 호출되는 데이터의 개수를 10개로 제한
        RestTemplate restTemplate = new RestTemplate();

        // 단일 요청
        String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/areaBasedList1")
                .queryParam("serviceKey", serviceKey)
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", pageNo)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "AppTest")
                .queryParam("arrange", "A")  // 제목순 정렬
                .queryParam("contentTypeId", "28")  // 레저스포츠의 contentTypeId
                .queryParam("_type", "json")
                .build()
                .toUriString();

        logger.info("단일 요청 URL: " + url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("응답 상태: " + response.getStatusCode());
            logger.info("응답 본문: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");

                if (itemsNode.isArray()) {
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
                        leisureSportsEvents.add(event);
                    }
                    leisureSportsEventRepository.saveAll(leisureSportsEvents);
                    logger.info("단일 요청으로 저장된 레저스포츠 이벤트 수: " + leisureSportsEvents.size());
                }
            }
        } catch (Exception e) {
            logger.severe("레저스포츠 이벤트 데이터를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
        return leisureSportsEvents;
    }

    //        //모든 데이터 불러오기
//        while (moreData) {
//            String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/areaBasedList1")
//                    .queryParam("serviceKey", serviceKey)
//                    .queryParam("numOfRows", numOfRows)
//                    .queryParam("pageNo", pageNo)
//                    .queryParam("MobileOS", "ETC")
//                    .queryParam("MobileApp", "AppTest")
//                    .queryParam("contentTypeId", "28")
//                    .queryParam("_type", "json")
//                    .build()
//                    .toUriString();
//
//            try {
//                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
//
//                if (response.getStatusCode().is2xxSuccessful()) {
//                    ObjectMapper objectMapper = new ObjectMapper();
//                    JsonNode rootNode = objectMapper.readTree(response.getBody());
//                    JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");
//
//                    if (itemsNode.isArray()) {
//                        List<LeisureSportsEvent> events = new ArrayList<>();
//                        for (JsonNode node : itemsNode) {
//                            LeisureSportsEvent event = new LeisureSportsEvent();
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
//                        leisureSportsEventRepository.saveAll(events);
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

    // LeisureSportsEventDetail 저장
    public void fetchAndSaveLeisureSportsEventDetail(String contentid) {
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

        logger.info("레저스포츠 이벤트 상세 정보 가져오기: contentId = " + contentid);
        logger.info("요청 URL: " + url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("API 응답: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode itemsNode = rootNode.path("response").path("body").path("items");

                if (itemsNode.isMissingNode() || !itemsNode.has("item")) {
                    logger.warning("해당 contentId에 대한 레저스포츠 이벤트 상세 정보가 없습니다: " + contentid);
                    return;
                }

                JsonNode itemNode = itemsNode.path("item").get(0);

                if (itemNode == null) {
                    logger.warning("해당 contentId에 대한 항목이 없습니다: " + contentid);
                    return;
                }

                LeisureSportsEventDetail eventDetail = new LeisureSportsEventDetail();
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

                leisureSportsEventDetailRepository.save(eventDetail);
                logger.info("레저스포츠 이벤트 상세 정보가 저장되었습니다: contentId = " + contentid);
            } else {
                logger.warning("해당 contentId에 대한 레저스포츠 이벤트 상세 정보를 가져오지 못했습니다: " + contentid);
            }
        } catch (Exception e) {
            logger.severe("contentId: " + contentid + "에 대한 레저스포츠 이벤트 상세 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
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

        logger.info("레저스포츠 이벤트 소개 정보 가져오기: contentId = " + contentid);
        logger.info("요청 URL: " + url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("API 응답: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readTree(response.getBody()).path("response").path("body").path("items").path("item").get(0);
            } else {
                logger.warning("해당 contentId에 대한 레저스포츠 이벤트 소개 정보를 가져오지 못했습니다: " + contentid);
            }
        } catch (Exception e) {
            logger.severe("contentId: " + contentid + "에 대한 레저스포츠 이벤트 소개 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
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

        logger.info("레저스포츠 이벤트 이미지 정보 가져오기: contentId = " + contentid);
        logger.info("요청 URL: " + url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("API 응답: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readTree(response.getBody()).path("response").path("body").path("items").path("item");
            } else {
                logger.warning("해당 contentId에 대한 레저스포츠 이벤트 이미지 정보를 가져오지 못했습니다: " + contentid);
            }
        } catch (Exception e) {
            logger.severe("contentId: " + contentid + "에 대한 레저스포츠 이벤트 이미지 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    //데이터베이스에서 여행 코스 상세 정보 추출
    public LeisureSportsEventDetail getLeisureSportsEventDetailFromDB(String contentid) {
        return leisureSportsEventDetailRepository.findByContentid(contentid);
    }
    public List<LeisureSportsEvent> getLeisureSportsEventsByCategory(String category) {
        // 카테고리 맵핑 로직에 따라 contentTypeId를 설정
        String contentTypeId = "28"; // 28 관광지 설정

        return leisureSportsEventRepository.findByContenttypeid(contentTypeId); // 필요에 따라 로직 변경
    }
    // '서울특별시'에 해당하는 레저스포츠 이벤트 가져오기
    public List<LeisureSportsEvent> getLeisureSportsByRegion(String region) {
        return leisureSportsEventRepository.findAll().stream()
                .filter(event -> event.getAddr1().contains(region))
                .collect(Collectors.toList());
    }

}
