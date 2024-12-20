package side.side.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.model.CulturalFacility;
import side.side.model.CulturalFacilityDetail;
import side.side.model.FoodEvent;
import side.side.model.TouristAttraction;
import side.side.repository.CulturalFacilityRepository;
import side.side.service.CulturalFacilityService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cultural-facilities")
public class CulturalFacilityController {

    @Autowired
    private CulturalFacilityService culturalFacilityService;
    @Autowired
    private CulturalFacilityRepository culturalFacilityRepository;

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
    // contentypeid가 14인 서비스 실행
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
    // 키워드 추출
    @GetMapping("/by-region")
    public List<CulturalFacility> getCulturalFacilityByRegion(@RequestParam String region) {
        return culturalFacilityService.getCulturalFacilityByRegion(region);
    }
    // 유사한 여행 코스 정보 가져오기
    @GetMapping("/{contenttypeid}/similar-events")
    public ResponseEntity<List<CulturalFacility>> getSimilarCulturalFacility(@PathVariable String contenttypeid) {
        List<CulturalFacility> similarEvents = culturalFacilityService.getSimilarCulturalFacility(contenttypeid);
        if (similarEvents.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(similarEvents);
    }
    // 퍼스트 이미지 가져오기
    @GetMapping("/firstimage/{contentid}")
    public ResponseEntity<String> fetchFirstImage(@PathVariable String contentid) {
        Optional<CulturalFacility> eventOptional = culturalFacilityRepository.findByContentid(contentid);
        if (eventOptional.isPresent()) {
            CulturalFacility event = eventOptional.get();
            return ResponseEntity.ok(event.getFirstimage());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
