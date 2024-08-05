package side.side.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import side.side.model.TravelCourse;
import side.side.model.TravelCourseDetail;
import side.side.repository.TravelCourseDetailRepository;
import side.side.repository.TravelCourseRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class TravelCourseService {

    private static final Logger logger = Logger.getLogger(TravelCourseService.class.getName());

    @Autowired
    private TravelCourseRepository travelCourseRepository;

    @Autowired
    private TravelCourseDetailRepository travelCourseDetailRepository;

    private final String serviceKey = "13jkaARutXp/OwAHynRnYjP7BJuMVGIZx2Ki3dRMaDlcBqrfZHC9Zk97LCCuLyKfiR2cVhyWy59t96rPwyWioA==";

    // contenttypeid가 25인 여행 코스를 가져와서 DB에 저장하는 메소드
    public List<TravelCourse> fetchAndSaveTravelCourses(String numOfRows, String pageNo) {
        List<TravelCourse> allCourses = new ArrayList<>();
        numOfRows = "10";  // 호출되는 데이터의 개수를 10개로 제한
        boolean moreData = true;
        RestTemplate restTemplate = new RestTemplate();

        // 단일 요청
        String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/areaBasedList1")
                .queryParam("serviceKey", serviceKey)
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", pageNo)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "AppTest")
                .queryParam("arrange", "A")
                .queryParam("contentTypeId", "25")  // contenttypeid 25로 설정
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
                    List<TravelCourse> courses = new ArrayList<>();
                    for (JsonNode node : itemsNode) {
                        TravelCourse course = new TravelCourse();
                        course.setTitle(node.path("title").asText());
                        course.setAddr1(node.path("addr1").asText());
                        course.setOverview(node.path("overview").asText());
                        course.setMapx(node.path("mapx").asText());
                        course.setMapy(node.path("mapy").asText());
                        course.setContentid(node.path("contentid").asText());
                        course.setContenttypeid(node.path("contenttypeid").asText());
                        course.setAreacode(node.path("areacode").asText());
                        course.setSigungucode(node.path("sigungucode").asText());
                        course.setZipcode(node.path("zipcode").asText());
                        course.setTel(node.path("tel").asText());
                        course.setModifiedtime(node.path("modifiedtime").asText());
                        courses.add(course);
                    }
                    travelCourseRepository.saveAll(courses);
                    allCourses.addAll(courses);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return allCourses;
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
//                    .queryParam("contentTypeId", "25")  // contenttypeid 25로 설정
//                    .queryParam("_type", "json")
//                    .build()
//                    .toUriString();
//
//            logger.info("요청 URL: " + url);
//
//            try {
//                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
//                logger.info("응답 상태: " + response.getStatusCode());
//                logger.info("응답 본문: " + response.getBody());
//
//                if (response.getStatusCode().is2xxSuccessful()) {
//                    ObjectMapper objectMapper = new ObjectMapper();
//                    JsonNode rootNode = objectMapper.readTree(response.getBody());
//                    JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");
//
//                    if (itemsNode.isArray()) {
//                        List<TravelCourse> courses = new ArrayList<>();
//                        for (JsonNode node : itemsNode) {
//                            TravelCourse course = new TravelCourse();
//                            course.setTitle(node.path("title").asText());
//                            course.setAddr1(node.path("addr1").asText());
//                            course.setOverview(node.path("overview").asText());
//                            course.setMapx(node.path("mapx").asText());
//                            course.setMapy(node.path("mapy").asText());
//                            course.setContentid(node.path("contentid").asText());
//                            course.setContenttypeid(node.path("contenttypeid").asText());
//                            course.setAreacode(node.path("areacode").asText());
//                            course.setSigungucode(node.path("sigungucode").asText());
//                            course.setZipcode(node.path("zipcode").asText());
//                            course.setTel(node.path("tel").asText());
//                            course.setModifiedtime(node.path("modifiedtime").asText());
//                            courses.add(course);
//                        }
//                        travelCourseRepository.saveAll(courses);
//                        allCourses.addAll(courses);
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
//        return allCourses;
//    }

    // TravelCourseDetail 저장
    public void fetchAndSaveTravelCourseDetail(String contentid) {
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

        logger.info("여행 코스 상세 정보 가져오기: contentId = " + contentid);
        logger.info("요청 URL: " + url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("API 응답: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode itemsNode = rootNode.path("response").path("body").path("items");

                if (itemsNode.isMissingNode() || !itemsNode.has("item")) {
                    logger.warning("해당 contentId에 대한 여행 코스 상세 정보가 없습니다: " + contentid);
                    return;
                }

                JsonNode itemNode = itemsNode.path("item").get(0);

                if (itemNode == null) {
                    logger.warning("해당 contentId에 대한 항목이 없습니다: " + contentid);
                    return;
                }

                TravelCourseDetail courseDetail = new TravelCourseDetail();
                courseDetail.setContentid(itemNode.path("contentid").asText());
                courseDetail.setContenttypeid(itemNode.path("contenttypeid").asText());
                courseDetail.setBooktour(itemNode.path("booktour").asText());
                courseDetail.setCreatedtime(itemNode.path("createdtime").asText());
                courseDetail.setHomepage(itemNode.path("homepage").asText());
                courseDetail.setModifiedtime(itemNode.path("modifiedtime").asText());
                courseDetail.setTel(itemNode.path("tel").asText());
                courseDetail.setTelname(itemNode.path("telname").asText());
                courseDetail.setTitle(itemNode.path("title").asText());
                courseDetail.setFirstimage(itemNode.path("firstimage").asText());
                courseDetail.setFirstimage2(itemNode.path("firstimage2").asText());
                courseDetail.setCpyrhtDivCd(itemNode.path("cpyrhtDivCd").asText());
                courseDetail.setAreacode(itemNode.path("areacode").asText());
                courseDetail.setSigungucode(itemNode.path("sigungucode").asText());
                courseDetail.setCat1(itemNode.path("cat1").asText());
                courseDetail.setCat2(itemNode.path("cat2").asText());
                courseDetail.setCat3(itemNode.path("cat3").asText());
                courseDetail.setAddr1(itemNode.path("addr1").asText());
                courseDetail.setAddr2(itemNode.path("addr2").asText());
                courseDetail.setZipcode(itemNode.path("zipcode").asText());
                courseDetail.setMapx(itemNode.path("mapx").asText());
                courseDetail.setMapy(itemNode.path("mapy").asText());
                courseDetail.setMlevel(itemNode.path("mlevel").asText());
                courseDetail.setOverview(itemNode.path("overview").asText());

                travelCourseDetailRepository.save(courseDetail);
                logger.info("여행 코스 상세 정보가 저장되었습니다: contentId = " + contentid);
            } else {
                logger.warning("해당 contentId에 대한 여행 코스 상세 정보를 가져오지 못했습니다: " + contentid);
            }
        } catch (Exception e) {
            logger.severe("contentId: " + contentid + "에 대한 여행 코스 상세 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
