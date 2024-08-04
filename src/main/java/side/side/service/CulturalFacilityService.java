package side.side.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import side.side.model.CulturalFacility;
import side.side.repository.CulturalFacilityRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class CulturalFacilityService {

    private static final Logger logger = Logger.getLogger(CulturalFacilityService.class.getName());

    @Autowired
    private CulturalFacilityRepository culturalFacilityRepository;

    private final String serviceKey = "13jkaARutXp/OwAHynRnYjP7BJuMVGIZx2Ki3dRMaDlcBqrfZHC9Zk97LCCuLyKfiR2cVhyWy59t96rPwyWioA==";

    // 문화시설 API 호출 및 데이터 저장
    public List<CulturalFacility> fetchAndSaveCulturalFacilities(String numOfRows, String pageNo) {
        List<CulturalFacility> allFacilities = new ArrayList<>();
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
                    .queryParam("contentTypeId", "14")  // 문화시설의 contentTypeId
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
                            List<CulturalFacility> facilities = new ArrayList<>();
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
                                facilities.add(facility);
                            }
                            culturalFacilityRepository.saveAll(facilities);
                            allFacilities.addAll(facilities);

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

        return allFacilities;
    }
}
