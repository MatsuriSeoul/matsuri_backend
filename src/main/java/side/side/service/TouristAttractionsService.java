package side.side.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import side.side.model.TouristAttraction;
import side.side.model.TouristAttractionDetail;
import side.side.repository.TouristAttractionDetailRepository;
import side.side.repository.TouristAttractionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class TouristAttractionsService {

    private static final Logger logger = Logger.getLogger(TouristAttractionsService.class.getName());


    @Autowired
    private TouristAttractionRepository touristAttractionRepository;

    @Autowired
    private TouristAttractionDetailRepository  touristAttractionDetailRepository;

    private final String serviceKey = "13jkaARutXp/OwAHynRnYjP7BJuMVGIZx2Ki3dRMaDlcBqrfZHC9Zk97LCCuLyKfiR2cVhyWy59t96rPwyWioA==";

    // contentTypeId 12인 관광지 db 저장하기
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

        logger.info("요청 URL: " + url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("응답 상태: " + response.getStatusCode());
            logger.info("응답 본문: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
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
                .build()
                .toUriString();

        logger.info("관광지 상세 정보 가져오기: contentId = " + contentid);
        logger.info("요청 URL: " + url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("API 응답: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");

                if (itemsNode.isArray()) {
                    for (JsonNode itemNode : itemsNode) {
                        TouristAttractionDetail attractionDetail = new TouristAttractionDetail();
                        attractionDetail.setContentid(itemNode.path("contentid").asText());
                        attractionDetail.setContenttypeid(itemNode.path("contenttypeid").asText());
                        attractionDetail.setBooktour(itemNode.path("booktour").asText());
                        attractionDetail.setCreatedtime(itemNode.path("createdtime").asText());
                        attractionDetail.setHomepage(itemNode.path("homepage").asText());
                        attractionDetail.setModifiedtime(itemNode.path("modifiedtime").asText());
                        attractionDetail.setTel(itemNode.path("tel").asText());
                        attractionDetail.setTelname(itemNode.path("telname").asText());
                        attractionDetail.setTitle(itemNode.path("title").asText());

                        attractionDetail.setFirstimage(itemNode.path("firstimage").asText(null));
                        attractionDetail.setFirstimage2(itemNode.path("firstimage2").asText(null));

                        attractionDetail.setCpyrhtDivCd(itemNode.path("cpyrhtDivCd").asText());
                        attractionDetail.setAreacode(itemNode.path("areacode").asText());
                        attractionDetail.setSigungucode(itemNode.path("sigungucode").asText());
                        attractionDetail.setCat1(itemNode.path("cat1").asText());
                        attractionDetail.setCat2(itemNode.path("cat2").asText());
                        attractionDetail.setCat3(itemNode.path("cat3").asText());
                        attractionDetail.setAddr1(itemNode.path("addr1").asText());
                        attractionDetail.setAddr2(itemNode.path("addr2").asText());
                        attractionDetail.setZipcode(itemNode.path("zipcode").asText());
                        attractionDetail.setMapx(itemNode.path("mapx").asText());
                        attractionDetail.setMapy(itemNode.path("mapy").asText());
                        attractionDetail.setMlevel(itemNode.path("mlevel").asText());
                        attractionDetail.setOverview(itemNode.path("overview").asText());

                        touristAttractionDetailRepository.save(attractionDetail);
                        logger.info("관광지 상세 정보가 저장되었습니다: contentId = " + contentid);
                    }
                } else {
                    logger.warning("해당 contentId에 대한 관광지 상세 정보가 없습니다: " + contentid);
                }
            } else {
                logger.warning("해당 contentId에 대한 관광지 상세 정보를 가져오지 못했습니다: " + contentid);
            }
        } catch (Exception e) {
            logger.severe("contentId: " + contentid + "에 대한 관광지 상세 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
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
    public TouristAttractionDetail getTouristAttractionDetailFromDB(String contentid) {
        return touristAttractionDetailRepository.findByContentid(contentid);
    }
    public List<TouristAttraction> getTouristAttractionsByCategory(String category) {
        // 카테고리 맵핑 로직에 따라 contentTypeId를 설정
        String contentTypeId = "12"; // 12 관광지 설정

        // 카테고리에 따른 관광지 데이터 가져오기
        return touristAttractionRepository.findByContenttypeid(contentTypeId);
    }
}
