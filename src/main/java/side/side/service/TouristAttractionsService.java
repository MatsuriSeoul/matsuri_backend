package side.side.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import side.side.model.TouristAttraction;
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

    private final String serviceKey = "13jkaARutXp/OwAHynRnYjP7BJuMVGIZx2Ki3dRMaDlcBqrfZHC9Zk97LCCuLyKfiR2cVhyWy59t96rPwyWioA==";

    // contentTypeId 12인 관광지 db 저장하기
    public List<TouristAttraction> fetchAndSaveTouristAttractions(String numOfRows, String pageNo) {
        List<TouristAttraction> allAttractions = new ArrayList<>();
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
                    .queryParam("contentTypeId", "12")
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

        return allAttractions;
    }
}
