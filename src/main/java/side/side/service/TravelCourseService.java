package side.side.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import side.side.model.TravelCourse;
import side.side.repository.TravelCourseRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class TravelCourseService {

    private static final Logger logger = Logger.getLogger(TravelCourseService.class.getName());

    @Autowired
    private TravelCourseRepository travelCourseRepository;  // TravelCourseRepository를 생성하여 주입합니다.

    private final String serviceKey = "13jkaARutXp/OwAHynRnYjP7BJuMVGIZx2Ki3dRMaDlcBqrfZHC9Zk97LCCuLyKfiR2cVhyWy59t96rPwyWioA==";

    // contenttypeid가 25인 여행 코스를 가져와서 DB에 저장하는 메소드
    public List<TravelCourse> fetchAndSaveTravelCourses(String numOfRows, String pageNo) {
        List<TravelCourse> allCourses = new ArrayList<>();
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
                    .queryParam("contentTypeId", "25")  // contenttypeid 25로 설정
                    .queryParam("_type", "json")
                    .build()
                    .toUriString();

            logger.info("Request URL: " + url);

            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                logger.info("Response Status: " + response.getStatusCode());
                logger.info("Response Body: " + response.getBody());

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

        return allCourses;
    }
}
