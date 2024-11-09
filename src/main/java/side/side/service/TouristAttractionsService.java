package side.side.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import side.side.model.*;
import side.side.repository.TouristAttractionDetailRepository;
import side.side.repository.TouristAttractionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class TouristAttractionsService {

    private static final Logger logger = Logger.getLogger(TouristAttractionsService.class.getName());


    @Autowired
    private TouristAttractionRepository touristAttractionRepository;

    @Autowired
    private TouristAttractionDetailRepository  touristAttractionDetailRepository;

    private final String serviceKey;

    public TouristAttractionsService(){
        Dotenv dotenv = Dotenv.load();
        this.serviceKey = dotenv.get("CATEGORY_API_KEY");
    }
    // contentTypeId 12인 관광지 db 저장하기
    @Transactional(propagation = Propagation.REQUIRED)
    public List<TouristAttraction> fetchAndSaveTouristAttractions(String numOfRows, String pageNo) {
        List<TouristAttraction> allAttractions = new ArrayList<>();
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
                .queryParam("arrange", "A")
                .queryParam("contentTypeId", "12")
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
                        TouristAttraction event = new TouristAttraction();
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


                        Optional<TouristAttraction> existingDetail = touristAttractionRepository.findByContentidForUpdate(event.getContentid());
                        if (existingDetail.isPresent()) {
                            continue;
                        }

                        touristAttractionRepository.upsertTouristAttraction(
                                event.getContentid(),
                                event.getTitle(),
                                event.getAddr1(),
                                event.getFirstimage(),
                                event.getMapx(),
                                event.getMapy(),
                                event.getContenttypeid()
                        );

                        allAttractions.add(event);
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allAttractions;
    }

    //모든 데이터 불러오기
//        while (moreData) {
//            String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/areaBasedList1")
//                    .queryParam("serviceKey", serviceKey)
//                    .queryParam("numOfRows", numOfRows)
//                    .queryParam("pageNo", pageNo)
//                    .queryParam("MobileOS", "ETC")
//                    .queryParam("MobileApp", "AppTest")
//                    .queryParam("arrange", "A")
//                    .queryParam("contentTypeId", "12")
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
//                            List<TouristAttraction> attractions = new ArrayList<>();
//                            for (JsonNode node : itemsNode) {
//                                TouristAttraction attraction = new TouristAttraction();
//                                attraction.setTitle(node.path("title").asText());
//                                attraction.setAddr1(node.path("addr1").asText());
//                                attraction.setFirstimage(node.path("firstimage").asText());
//                                attraction.setMapx(node.path("mapx").asText());
//                                attraction.setMapy(node.path("mapy").asText());
//                                attraction.setOverview(node.path("overview").asText());
//                                attraction.setContentid(node.path("contentid").asText());
//                                attraction.setContenttypeid(node.path("contenttypeid").asText());
//                                attraction.setAreacode(node.path("areacode").asText());
//                                attraction.setSigungucode(node.path("sigungucode").asText());
//                                attraction.setZipcode(node.path("zipcode").asText());
//                                attraction.setTel(node.path("tel").asText());
//                                attraction.setModifiedtime(node.path("modifiedtime").asText());
//                                attractions.add(attraction);
//                            }
//                            touristAttractionRepository.saveAll(attractions);
//                            allAttractions.addAll(attractions);
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
//        return allAttractions;
//    }
    // TouristAttractionDetail 저장
    @Transactional(propagation = Propagation.REQUIRED)
    public void fetchAndSaveTouristAttractionDetail(String contentid) {
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
                .queryParam("mapinfoYN", "Y")
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

                TouristAttractionDetail eventDetail = new TouristAttractionDetail();
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
                Optional<TouristAttractionDetail> existingDetail = touristAttractionDetailRepository.findByContentidForUpdate(contentid);
                if (existingDetail.isPresent()) {
                    return;
                }

                // Upsert 사용하여 데이터 삽입 또는 업데이트
                touristAttractionDetailRepository.upsertTouristAttractionDetail(
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



    // contentId와 contentTypeId로 외부 API에서 소개 정보 가져오기
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

        logger.info("소개 정보 가져오기: contentId = " + contentid + ", contentTypeId = " + contenttypeid);
        logger.info("요청 URL: " + url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("API 응답: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");

                if (itemsNode.isArray() && itemsNode.size() > 0) {
                    return itemsNode.get(0);
                } else {
                    logger.warning("소개 정보가 없습니다: contentId = " + contentid);
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

    // 이미지 정보 조회 API 호출 contentid로 외부에서 api 호출
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

        logger.info("이미지 데이터 가져오는 중: contentId = " + contentid);
        logger.info("요청 URL: " + url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("API 응답: " + response.getBody());

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
                logger.warning("해당 contentId에 대한 이미지 가져오기에 실패했습니다: " + contentid);
                return null;
            }
        } catch (Exception e) {
            logger.severe("contentId: " + contentid + "에 대한 이미지 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // contentId로 데이터베이스에서 저장된 관광지 상세 정보 가져오기
    @Transactional
    public TouristAttractionDetail getTouristAttractionDetailFromDB(String contentid) {
        TouristAttractionDetail detail = touristAttractionDetailRepository.findByContentid(contentid);
        if (detail == null) {
            System.out.println("No TouristAttractionDetail found for contentid: " + contentid);
        } else {
            System.out.println("TouristAttractionDetail found: " + detail);
        }
        return detail;
    }

    @Transactional
    public List<TouristAttraction> getTouristAttractionsByCategory(String category) {
        // 카테고리 맵핑 로직에 따라 contentTypeId를 설정
        String contentTypeId = "12"; // 12 관광지 설정

        // 카테고리에 따른 관광지 데이터 가져오기
        return touristAttractionRepository.findByContenttypeid(contentTypeId);
    }

    // '서울특별시'에 해당하는 관광지 이벤트 가져오기
    @Transactional
    public List<TouristAttraction> getTouristAttractionByRegion(String region) {
        return touristAttractionRepository.findAll().stream()
                .filter(event -> event.getAddr1().contains(region))
                .collect(Collectors.toList());
    }
    // 유사한 여행지 정보 가져오기
    @Transactional
    public List<TouristAttraction> getSimilarTouristAttractions(String contenttypeid) {
        return touristAttractionRepository.findByContenttypeid(contenttypeid);
    }

    // contentid로 TouristAttraction 조회
    @Transactional
    public List<TouristAttraction> findBycontentid(String contentid) {
        return touristAttractionRepository.findBycontentid(contentid);
    }
}
