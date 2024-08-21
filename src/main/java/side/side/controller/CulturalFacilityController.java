package side.side.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.model.CulturalFacility;
import side.side.model.CulturalFacilityDetail;
import side.side.service.CulturalFacilityService;

import java.util.List;

@RestController
@RequestMapping("/api/cultural-facilities")
public class CulturalFacilityController {

    @Autowired
    private CulturalFacilityService culturalFacilityService;

    // 문화시설 데이터 가져오기 및 저장
    @GetMapping("/fetchAndSaveCulturalFacilities")
    public ResponseEntity<List<CulturalFacility>> fetchAndSaveCulturalFacilities(
            @RequestParam String numOfRows,
            @RequestParam String pageNo) {
        List<CulturalFacility> facilities = culturalFacilityService.fetchAndSaveCulturalFacilities(numOfRows, pageNo);
        return ResponseEntity.ok(facilities);
    }

    // 문화시설 상세 정보 가져오기
    @GetMapping("/{contentid}/detail")
    public ResponseEntity<?> getCulturalFacilityDetail(@PathVariable String contentid) {
        CulturalFacilityDetail detail = culturalFacilityService.getCulturalFacilityDetailFromDB(contentid);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }

    // 외부 API에서 소개 정보 가져오기
    @GetMapping("/{contentid}/{contenttypeid}/intro")
    public ResponseEntity<?> fetchIntroInfo(@PathVariable String contentid, @PathVariable String contenttypeid) {
        JsonNode introInfo = culturalFacilityService.fetchIntroInfoFromApi(contentid, contenttypeid);
        if (introInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(introInfo);
    }

    // 외부 API에서 이미지 정보 가져오기
    @GetMapping("/{contentid}/images")
    public ResponseEntity<JsonNode> fetchImages(@PathVariable String contentid) {
        JsonNode images = culturalFacilityService.fetchImagesFromApi(contentid);
        if (images == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(images);

    }
}