package side.side.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import side.side.model.ShoppingEvent;
import side.side.repository.ShoppingEventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class ShoppingEventService {

    private static final Logger logger = Logger.getLogger(ShoppingEventService.class.getName());

    @Autowired
    private ShoppingEventRepository shoppingEventRepository;

    private final String serviceKey = "13jkaARutXp/OwAHynRnYjP7BJuMVGIZx2Ki3dRMaDlcBqrfZHC9Zk97LCCuLyKfiR2cVhyWy59t96rPwyWioA==";

    // 쇼핑 API 호출 및 데이터 저장
    public List<ShoppingEvent> fetchAndSaveShoppingEvents(String numOfRows, String pageNo) {
        List<ShoppingEvent> allShoppingEvents = new ArrayList<>();
        boolean moreData = true;
        RestTemplate restTemplate = new RestTemplate();

        while (moreData) {
            String url = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/areaBasedList1")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("numOfRows", numOfRows)
                    .queryParam("pageNo", pageNo)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "AppTest")
                    .queryParam("arrange", "A")  // 제목순 정렬
                    .queryParam("contentTypeId", "38")  // 쇼핑의 contentTypeId
                    .queryParam("_type", "json")
                    .build()
                    .toUriString();

            logger.info("Request URL: " + url);

            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                logger.info("Response Status: " + response.getStatusCode());
                logger.info("Response Body: " + response.getBody());

                if (response.getStatusCode().is2xxSuccessful()) {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode rootNode = objectMapper.readTree(response.getBody());
                        JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");

                        if (itemsNode.isArray()) {
                            List<ShoppingEvent> shoppingEvents = new ArrayList<>();
                            for (JsonNode node : itemsNode) {
                                ShoppingEvent event = new ShoppingEvent();
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
                                shoppingEvents.add(event);
                            }
                            shoppingEventRepository.saveAll(shoppingEvents);
                            allShoppingEvents.addAll(shoppingEvents);

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

        return allShoppingEvents;
    }
}
