package side.side.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import side.side.model.LocalEvent;
import side.side.repository.LocalEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class LocalEventService {

    private static final Logger logger = Logger.getLogger(LocalEventService.class.getName());

    @Autowired
    private LocalEventRepository localEventRepository;

    private final String serviceKey = "13jkaARutXp/OwAHynRnYjP7BJuMVGIZx2Ki3dRMaDlcBqrfZHC9Zk97LCCuLyKfiR2cVhyWy59t96rPwyWioA==";

    // 지역 기반 관광 정보 API - 로컬 데이터 저장 / LocalEvent
    public List<LocalEvent> fetchAndSaveEventsLocal(String numOfRows, String pageNo) {
        List<LocalEvent> allEvents = new ArrayList<>();
        boolean moreData = true;
        RestTemplate restTemplate = new RestTemplate();

        while (moreData) {
            String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/areaBasedList1")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("numOfRows", numOfRows)
                    .queryParam("pageNo", pageNo)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "AppTest")
                    .queryParam("arrange", "A")  // 기본 정렬을 제목순으로 설정
                    .queryParam("contentTypeId", "32")
                    .queryParam("_type", "json")
                    .build()
                    .toUriString();

            logger.info("Request URL: " + url);  // Request URL을 로깅

            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                logger.info("Response Status: " + response.getStatusCode());
                logger.info("Response Body: " + response.getBody());  // Response Body를 로깅

                if (response.getStatusCode().is2xxSuccessful()) {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode rootNode = objectMapper.readTree(response.getBody());
                        JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");

                        if (itemsNode.isArray()) {
                            List<LocalEvent> events = new ArrayList<>();
                            for (JsonNode node : itemsNode) {
                                LocalEvent event = new LocalEvent();
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
                                events.add(event);
                            }
                            localEventRepository.saveAll(events);  // LocalEvent로 저장
                            allEvents.addAll(events);

                            if (itemsNode.size() < Integer.parseInt(numOfRows)) {
                                moreData = false;
                            } else {
                                pageNo = String.valueOf(Integer.parseInt(pageNo) + 1);
                            }
                        } else {
                            moreData = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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

        return allEvents;
    }
}
