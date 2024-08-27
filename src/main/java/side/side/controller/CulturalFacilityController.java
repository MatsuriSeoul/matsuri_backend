package side.side.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.model.CulturalFacility;
import side.side.model.CulturalFacilityDetail;
import side.side.model.TouristAttraction;
import side.side.service.CulturalFacilityService;

import java.util.List;

@RestController
@RequestMapping("/api/cultural-facilities")
public class CulturalFacilityController {

    @Autowired
    private CulturalFacilityService culturalFacilityService;

    // 문화시설 상세 정보 불러오기
    @GetMapping("/{contentid}/detail")
    public ResponseEntity<?> getCulturalFacilityDetail(@PathVariable String contentid) {
        CulturalFacilityDetail detail = culturalFacilityService.getCulturalFacilityDetailFromDB(contentid);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }

    // 문화시설 소개 정보 불러오기 (외부 API에서)
    @GetMapping("/{contentid}/{contenttypeid}/intro")
    public ResponseEntity<?> getIntroInfo(@PathVariable String contentid, @PathVariable String contenttypeid) {
        JsonNode introInfo = culturalFacilityService.fetchIntroInfoFromApi(contentid, contenttypeid);
        if (introInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(introInfo);
    }

    // 이미지 정보 조회 불러오기 (외부 API에서)
    @GetMapping("/{contentid}/images")
    public ResponseEntity<?> getImages(@PathVariable String contentid) {
        JsonNode images = culturalFacilityService.fetchImagesFromApi(contentid);
        if (images == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(images);
    }
    // 카테고리별 문화시설 가져오기
    @GetMapping("/category/{category}")
    public ResponseEntity<List<CulturalFacility>> getCulturalFacilitiesByCategory(@PathVariable String category) {
        List<CulturalFacility> facilities = culturalFacilityService.getCulturalFacilitiesByCategory(category);
        if (facilities.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(facilities);
    }
    // 관광지 카테고리 데이터 불러오기
    @GetMapping("/fetchAndSaveCulturalFacilities")
    public ResponseEntity<List<CulturalFacility>> fetchAndSaveTouristAttractions(
            @RequestParam String numOfRows,
            @RequestParam String pageNo) {

        List<CulturalFacility> attractions = culturalFacilityService.fetchAndSaveCulturalFacilities(numOfRows, pageNo);
        return ResponseEntity.ok(attractions);
    }
}
