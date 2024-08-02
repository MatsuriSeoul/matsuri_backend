package side.side.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import side.side.model.TourCourse;
import side.side.repository.TourCourseRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class TourCourseService {

    private static final Logger logger = Logger.getLogger(TourCourseService.class.getName());

    @Autowired
    private TourCourseRepository tourCourseRepository;

    private final String serviceKey = "13jkaARutXp/OwAHynRnYjP7BJuMVGIZx2Ki3dRMaDlcBqrfZHC9Zk97LCCuLyKfiR2cVhyWy59t96rPwyWioA==";


    // 여행 코스 데이터를 가져와 DB에 저장
    public List<TourCourse> fetchAndSaveTourCourses(String numOfRows, String pageNo, String contentId) {
        List<TourCourse> allCourses = new ArrayList<>();
        boolean moreData = true;
        RestTemplate restTemplate = new RestTemplate();

        while (moreData) {
            String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/detailInfo1")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("numOfRows", numOfRows)
                    .queryParam("pageNo", pageNo)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "AppTest")
                    .queryParam("contentId", contentId)
                    .queryParam("contentTypeId", "25")
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
                        List<TourCourse> courses = new ArrayList<>();
                        for (JsonNode node : itemsNode) {
                            TourCourse course = new TourCourse();
                            course.setContentid(node.path("contentid").asText());
                            course.setTitle(node.path("title").asText());
                            course.setSubname(node.path("subname").asText());
                            course.setSubdetailoverview(node.path("subdetailoverview").asText());
                            course.setSubdetailimg(node.path("subdetailimg").asText());
                            course.setSubdetailalt(node.path("subdetailalt").asText());
                            course.setDistance(node.path("distance").asText());
                            course.setTaketime(node.path("taketime").asText());
                            course.setInfocentertourcourse(node.path("infocentertourcourse").asText());
                            course.setSchedule(node.path("schedule").asText());
                            courses.add(course);
                        }
                        tourCourseRepository.saveAll(courses);
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
