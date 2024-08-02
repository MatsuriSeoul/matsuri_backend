package side.side.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import side.side.model.LeisureSportsEvent;
import side.side.repository.LeisureSportsEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class LeisureSportsEventService {

    private static final Logger logger = Logger.getLogger(LeisureSportsEventService.class.getName());

    @Autowired
    private LeisureSportsEventRepository leisureSportsEventRepository;

    private final String serviceKey = "13jkaARutXp/OwAHynRnYjP7BJuMVGIZx2Ki3dRMaDlcBqrfZHC9Zk97LCCuLyKfiR2cVhyWy59t96rPwyWioA==";

    // ContentypeId 28인 레포츠 불러와 저장하기
    public List<LeisureSportsEvent> fetchAndSaveLeisureSportsEvents(String numOfRows, String pageNo) {
        List<LeisureSportsEvent> allEvents = new ArrayList<>();
        boolean moreData = true;
        RestTemplate restTemplate = new RestTemplate();

        while (moreData) {
            String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/areaBasedList1")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("numOfRows", numOfRows)
                    .queryParam("pageNo", pageNo)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "AppTest")
                    .queryParam("contentTypeId", "28")
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
                        List<LeisureSportsEvent> events = new ArrayList<>();
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
                            events.add(event);
                        }
                        leisureSportsEventRepository.saveAll(events);
                        allEvents.addAll(events);

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

        return allEvents;
    }
}

