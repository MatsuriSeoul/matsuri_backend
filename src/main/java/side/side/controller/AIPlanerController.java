package side.side.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AIPlanerController {

    //카테고리, 엔드포인트 매핑 map
    private final Map<String, String> categoryEndpointMap = new HashMap<>() {{
        put("관광지", "tourist-attractions");
        put("문화시설", "cultural-facilities");
        put("행사", "events");
        put("여행코스", "travel-courses");
        put("레포츠", "leisure-sports");
        put("숙박", "local-events");
        put("쇼핑", "shopping-events");
        put("음식", "food-events");
    }};

    //인코딩 해제
    private String getDecodedCategory(String encodedCategory) {
        return UriUtils.decode(encodedCategory, StandardCharsets.UTF_8);
    }

    private String getEndpointByCategory(String decodedCategory) {
        return categoryEndpointMap.getOrDefault(decodedCategory, "default-endpoint");
    }



    //상세 정보 엔드포인트
    @GetMapping("/{encodedCategory}/{contentid}/detail")
    public ResponseEntity<?> getEventDetail(
            @PathVariable String encodedCategory,
            @PathVariable String contentid) {

        String decodedCategory = getDecodedCategory(encodedCategory);
        String endpoint = getEndpointByCategory(decodedCategory);

        Map<String, Object> response = new HashMap<>();
        response.put("endpoint", endpoint);
        response.put("decodedCategory", decodedCategory);
        response.put("contentid", contentid);

        return ResponseEntity.ok(response);
    }

    //소개 정보 엔드포인트
    @GetMapping("/{encodedCategory}/{contentid}/{contenttypeid}/intro")
    public ResponseEntity<?> getEventIntro(
            @PathVariable String encodedCategory,
            @PathVariable String contentid,
            @PathVariable String contenttypeid) {


        String decodedCategory = getDecodedCategory(encodedCategory);
        String endpoint = getEndpointByCategory(decodedCategory);

        Map<String, Object> response = new HashMap<>();
        response.put("endpoint", endpoint);
        response.put("decodedCategory", decodedCategory);
        response.put("contentid", contentid);
        response.put("contenttypeid", contenttypeid);

        return ResponseEntity.ok(response);
    }

    //상세 정보 엔드포인트
    @GetMapping("/{encodedCategory}/{contentid}/images")
    public ResponseEntity<?> getEventImage(
            @PathVariable String encodedCategory,
            @PathVariable String contentid) {

        String decodedCategory = getDecodedCategory(encodedCategory);
        String endpoint = getEndpointByCategory(decodedCategory);

        Map<String, Object> response = new HashMap<>();
        response.put("endpoint", endpoint);
        response.put("decodedCategory", decodedCategory);
        response.put("contentid", contentid);

        return ResponseEntity.ok(response);

    }
}
