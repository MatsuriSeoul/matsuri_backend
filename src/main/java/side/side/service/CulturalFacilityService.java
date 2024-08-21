package side.side.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import side.side.model.CulturalFacility;
import side.side.model.CulturalFacilityDetail;
import side.side.model.TouristAttraction;
import side.side.model.TouristAttractionDetail;
import side.side.repository.CulturalFacilityDetailRepository;
import side.side.repository.CulturalFacilityRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class CulturalFacilityService {

    private static final Logger logger = Logger.getLogger(CulturalFacilityService.class.getName());

    @Autowired
    private CulturalFacilityRepository culturalFacilityRepository;

    @Autowired
    private CulturalFacilityDetailRepository culturalFacilityDetailRepository;

    private final String serviceKey = "13jkaARutXp/OwAHynRnYjP7BJuMVGIZx2Ki3dRMaDlcBqrfZHC9Zk97LCCuLyKfiR2cVhyWy59t96rPwyWioA==";

    // 문화시설 API 호출 및 데이터 저장
    public List<CulturalFacility> fetchAndSaveCulturalFacilities(String numOfRows, String pageNo) {
        List<CulturalFacility> culturalFacilities = new ArrayList<>();
        numOfRows = "10";  // 호출되는 데이터의 개수를 10개로 제한
        RestTemplate restTemplate = new RestTemplate();

        String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/areaBasedList1")
                .queryParam("serviceKey", serviceKey)
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", pageNo)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "AppTest")
                .queryParam("arrange", "A")  // 제목순 정렬
                .queryParam("contentTypeId", "14")  // 문화시설의 contentTypeId
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
                        CulturalFacility facility = new CulturalFacility();
                        facility.setTitle(node.path("title").asText());
                        facility.setAddr1(node.path("addr1").asText());
                        facility.setFirstimage(node.path("firstimage").asText());
                        facility.setMapx(node.path("mapx").asText());
                        facility.setMapy(node.path("mapy").asText());
                        facility.setContentid(node.path("contentid").asText());
                        facility.setContenttypeid(node.path("contenttypeid").asText());
                        facility.setAreacode(node.path("areacode").asText());
                        facility.setSigungucode(node.path("sigungucode").asText());
                        facility.setTel(node.path("tel").asText());
                        facility.setOverview(node.path("overview").asText());
                        culturalFacilities.add(facility);
                    }
                    culturalFacilityRepository.saveAll(culturalFacilities);
                    logger.info("단일 요청으로 저장된 문화시설 수: " + culturalFacilities.size());
                }
            }
        } catch (Exception e) {
            logger.severe("문화시설 데이터를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }

        return culturalFacilities;
    }

    //        //모든 데이터 받아오기
//        while (moreData) {
//            String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/areaBasedList1")
//                    .queryParam("serviceKey", serviceKey)
//                    .queryParam("numOfRows", numOfRows)
//                    .queryParam("pageNo", pageNo)
//                    .queryParam("MobileOS", "ETC")
//                    .queryParam("MobileApp", "AppTest")
//                    .queryParam("arrange", "A")  // 제목순 정렬
//                    .queryParam("contentTypeId", "14")  // 문화시설의 contentTypeId
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
//                            List<CulturalFacility> facilities = new ArrayList<>();
//                            for (JsonNode node : itemsNode) {
//                                CulturalFacility facility = new CulturalFacility();
//                                facility.setTitle(node.path("title").asText());
//                                facility.setAddr1(node.path("addr1").asText());
//                                facility.setFirstimage(node.path("firstimage").asText());
//                                facility.setMapx(node.path("mapx").asText());
//                                facility.setMapy(node.path("mapy").asText());
//                                facility.setContentid(node.path("contentid").asText());
//                                facility.setContenttypeid(node.path("contenttypeid").asText());
//                                facility.setAreacode(node.path("areacode").asText());
//                                facility.setSigungucode(node.path("sigungucode").asText());
//                                facility.setTel(node.path("tel").asText());
//                                facility.setOverview(node.path("overview").asText());
//                                facilities.add(facility);
//                            }
//                            culturalFacilityRepository.saveAll(facilities);
//                            allFacilities.addAll(facilities);
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
//        return allFacilities;
//    }
    // 문화시설 상세 정보 저장
    public void fetchAndSaveCulturalFacilityDetail(String contentid) {
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

        logger.info("문화시설 상세 정보 가져오기: contentId = " + contentid);
        logger.info("요청 URL: " + url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("API 응답: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode itemsNode = rootNode.path("response").path("body").path("items");

                if (itemsNode.isMissingNode() || !itemsNode.has("item")) {
                    logger.warning("해당 contentId에 대한 문화시설 상세 정보가 없습니다: " + contentid);
                    return;
                }

                JsonNode itemNode = itemsNode.path("item").get(0);

                if (itemNode == null) {
                    logger.warning("해당 contentId에 대한 항목이 없습니다: " + contentid);
                    return;
                }

                CulturalFacilityDetail facilityDetail = new CulturalFacilityDetail();
                facilityDetail.setContentid(itemNode.path("contentid").asText());
                facilityDetail.setContenttypeid(itemNode.path("contenttypeid").asText());
                facilityDetail.setBooktour(itemNode.path("booktour").asText());
                facilityDetail.setCreatedtime(itemNode.path("createdtime").asText());
                facilityDetail.setHomepage(itemNode.path("homepage").asText());
                facilityDetail.setModifiedtime(itemNode.path("modifiedtime").asText());
                facilityDetail.setTel(itemNode.path("tel").asText());
                facilityDetail.setTelname(itemNode.path("telname").asText());
                facilityDetail.setTitle(itemNode.path("title").asText());
                facilityDetail.setFirstimage(itemNode.path("firstimage").asText());
                facilityDetail.setFirstimage2(itemNode.path("firstimage2").asText());
                facilityDetail.setCpyrhtDivCd(itemNode.path("cpyrhtDivCd").asText());
                facilityDetail.setAreacode(itemNode.path("areacode").asText());
                facilityDetail.setSigungucode(itemNode.path("sigungucode").asText());
                facilityDetail.setCat1(itemNode.path("cat1").asText());
                facilityDetail.setCat2(itemNode.path("cat2").asText());
                facilityDetail.setCat3(itemNode.path("cat3").asText());
                facilityDetail.setAddr1(itemNode.path("addr1").asText());
                facilityDetail.setAddr2(itemNode.path("addr2").asText());
                facilityDetail.setZipcode(itemNode.path("zipcode").asText());
                facilityDetail.setMapx(itemNode.path("mapx").asText());
                facilityDetail.setMapy(itemNode.path("mapy").asText());
                facilityDetail.setMlevel(itemNode.path("mlevel").asText());
                facilityDetail.setOverview(itemNode.path("overview").asText());

                culturalFacilityDetailRepository.save(facilityDetail);
                logger.info("문화시설 상세 정보가 저장되었습니다: contentId = " + contentid);
            } else {
                logger.warning("해당 contentId에 대한 문화시설 상세 정보를 가져오지 못했습니다: " + contentid);
            }
        } catch (Exception e) {
            logger.severe("contentId: " + contentid + "에 대한 문화시설 상세 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //문화시설 소개 정보 api 외부에서 호출
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
                    return itemsNode.get(0); // 첫 번째 아이템만 리턴
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

    //문화시설 이미지 정보 api
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

    //contentid로 데이터베이스에서 저장된 문화시설 상세 정보 가져오기
    public CulturalFacilityDetail getCulturalFacilityDetailFromDB(String contentid) {
        CulturalFacilityDetail detail = culturalFacilityDetailRepository.findByContentid(contentid);
        if (detail == null) {
            logger.warning("No CulturalFacilityDetail found for contentid: " + contentid);
        } else {
            logger.info("CulturalFacilityDetail retrieved: " + detail.toString());
        }
        return detail;
    }

    public List<CulturalFacility> getCulturalFacilityByCategory(String category) {
        // 카테고리 맵핑 로직에 따라 contentTypeId를 설정
        String contentTypeId = "14"; // 14 관광지 설정

        // 카테고리에 따른 관광지 데이터 가져오기
        return culturalFacilityRepository.findByContenttypeid(contentTypeId);
    }
}
