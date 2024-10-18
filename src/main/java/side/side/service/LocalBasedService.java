package side.side.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import side.side.model.CulturalFacility;
import side.side.model.LocalBase;
import side.side.repository.LocalBasedRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LocalBasedService {

    @Autowired
    private LocalBasedRepository localBasedRepository;

    private final String serviceKey = "13jkaARutXp/OwAHynRnYjP7BJuMVGIZx2Ki3dRMaDlcBqrfZHC9Zk97LCCuLyKfiR2cVhyWy59t96rPwyWioA==";
    private final String baseUrl = "http://apis.data.go.kr/B551011/KorService1/areaBasedList1";

    // API를 호출하고 결과를 저장하는 메소드
    public List<LocalBase> fetchAndSaveEvents(String region, int sigunguCode, int numOfRows, String pageNo) {
        List<LocalBase> allEvents = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();

        // API 호출 URL 빌드
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("serviceKey", serviceKey)
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", pageNo)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "AppTest")
                .queryParam("listYN", "Y")
                .queryParam("arrange", "A")
                .queryParam("areaCode", getAreaCode(region))
                .queryParam("sigunguCode", sigunguCode)
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
                    for (JsonNode node : itemsNode) {
                        LocalBase event = new LocalBase();
                        event.setTitle(node.path("title").asText());
                        event.setAddr1(node.path("addr1").asText());
                        event.setAddr2(node.path("addr2").asText());
                        event.setFirstImage(node.path("firstimage").asText());
                        event.setFirstImage2(node.path("firstimage2").asText());
                        event.setAreaCode(node.path("areacode").asInt());
                        event.setSigunguCode(node.path("sigungucode").asInt());
                        event.setCat1(node.path("cat1").asText());
                        event.setCat2(node.path("cat2").asText());
                        event.setCat3(node.path("cat3").asText());
                        event.setContentid(node.path("contentid").asText());
                        event.setContentTypeId(node.path("contenttypeid").asText());
                        event.setMapX(node.path("mapx").asDouble());
                        event.setMapY(node.path("mapy").asDouble());
                        event.setTelephone(node.path("tel").asText());
                        event.setZipcode(node.path("zipcode").asText());

                        // 중복을 무시하고 저장
                        localBasedRepository.insertIgnoreDuplicate(event);
                        allEvents.add(event);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return allEvents;
    }

    // 2. 실시간으로 상세 정보 API 호출 메소드 (저장하지 않음)
    public JsonNode fetchEventDetail(String contentid) {
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

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode itemNode = rootNode.path("response").path("body").path("items").path("item");

                if (itemNode.isMissingNode()) {
                    return null;
                }

                return itemNode;  // 상세 정보 반환
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 3. 실시간으로 이미지 정보 API 호출 메소드 (저장하지 않음)
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

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");

                if (itemsNode.isArray() && itemsNode.size() > 0) {
                    return itemsNode;  // 이미지 리스트 반환
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 4. 실시간으로 소개 정보 API 호출 메소드 (저장하지 않음)
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

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");

                if (itemsNode.isArray() && itemsNode.size() > 0) {
                    return itemsNode.get(0);  // 첫 번째 아이템 반환
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private int getAreaCode(String region) {
        switch (region.toLowerCase()) {
            case "seoul": return 1;
            case "busan": return 6;
            case "daegu": return 4;
            case "incheon": return 2;
            case "gwangju": return 5;
            case "daejeon": return 3;
            case "ulsan": return 7;
            case "sejong": return 8;
            case "gyeonggi": return 31;
            case "gangwon": return 32;
            case "chungbuk": return 33;
            case "chungnam": return 34;
            case "gyeongbuk": return 35;
            case "gyeongnam": return 36;
            case "jeonbuk": return 37;
            case "jeonnam": return 38;
            case "jeju": return 39;
            default: throw new IllegalArgumentException("Invalid region name: " + region);
        }
    }

    // 지역과 시군구 코드로 이벤트를 조회하는 메서드 추가
    public List<LocalBase> getEventsByAreaAndSigungu(int areaCode, int sigunguCode) {
        return localBasedRepository.findByAreaCodeAndSigunguCode(areaCode, sigunguCode);
    }
    // 특정 지역의 시군구 코드를 가져오는 메서드
    public List<Integer> getSigunguCodesByAreaCode(int areaCode) {
        return localBasedRepository.findDistinctSigunguCodesByAreaCode(areaCode);
    }
    // 유사한 이벤트를 가져오는 메소드
    public List<LocalBase> getSimilarEventsByContentType(String contenttypeid) {
        return localBasedRepository.findByContentTypeId(contenttypeid);
    }

    // contentid로 LocalBase 조회
    @Transactional
    public List<LocalBase> findBycontentid(String contentid) {
        return localBasedRepository.findBycontentid(contentid);
    }
}
