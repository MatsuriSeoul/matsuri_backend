package side.side.controller;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.model.TouristAttraction;
import side.side.model.TouristAttractionDetail;
import side.side.repository.TouristAttractionRepository;
import side.side.service.TouristAttractionsService;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

@RestController
@RequestMapping("/api/tourist-attractions")
public class TouristAttractionController {

    @Autowired
    private TouristAttractionsService touristAttractionsService;
    @Autowired
    private TouristAttractionRepository touristAttractionRepository;

    //  관광지 상세 정보 불러오기
    @GetMapping("/{contentid}/detail")
    public ResponseEntity<?> getTouristAttractionDetail(@PathVariable String contentid) {
        TouristAttractionDetail detail = touristAttractionsService.getTouristAttractionDetailFromDB(contentid);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }

    // 관광지 소개 정보 불러오기 (외부 API에서)
    @GetMapping("/{contentid}/{contenttypeid}/intro")
    public ResponseEntity<?> getIntroInfo(@PathVariable String contentid, @PathVariable String contenttypeid) {
        JsonNode introInfo = touristAttractionsService.fetchIntroInfoFromApi(contentid, contenttypeid);
        if (introInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(introInfo);
    }
    // 이미지 정보 조회 불러오기 (외부 API에서)
    @GetMapping("/{contentid}/images")
    public ResponseEntity<?> getImages(@PathVariable String contentid) {
        JsonNode images = touristAttractionsService.fetchImagesFromApi(contentid);
        if (images == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(images);
    }

    // contentypeid가 12인 서비스 실행
    @GetMapping("/category/{category}")
    public ResponseEntity<List<TouristAttraction>> getTouristAttractionsByCategory(@PathVariable String category) {
        List<TouristAttraction> attractions = touristAttractionsService.getTouristAttractionsByCategory(category);
        return ResponseEntity.ok(attractions);
    }
    // 키워드 추출
    @GetMapping("/by-region")
    public List<TouristAttraction> getTouristAttractionByRegion(@RequestParam String region) {
        return touristAttractionsService.getTouristAttractionByRegion(region);
    }
    // 유사한 관광지 정보 가져오기
    @GetMapping("/{contenttypeid}/similar-events")
    public ResponseEntity<List<TouristAttraction>> getSimilarTouristAttractions(@PathVariable String contenttypeid) {
        List<TouristAttraction> similarEvents = touristAttractionsService.getSimilarTouristAttractions(contenttypeid);
        if (similarEvents.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(similarEvents);
    }
}