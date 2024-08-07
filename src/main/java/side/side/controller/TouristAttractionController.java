package side.side.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.model.TouristAttraction;
import side.side.model.TouristAttractionDetail;
import side.side.service.TouristAttractionsService;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

@RestController
@RequestMapping("/api/tourist-attractions")
public class TouristAttractionController {

    @Autowired
    private TouristAttractionsService touristAttractionsService;

    @GetMapping("/{contentid}/detail")
    public ResponseEntity<?> getTouristAttractionDetail(@PathVariable String contentid) {
        TouristAttractionDetail detail = touristAttractionsService.getTouristAttractionDetailFromDB(contentid);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }

    // 관광지 상세 정보와 소개 정보를 담는 DTO 클래스 정의
    static class TouristAttractionDetailResponse {
        private TouristAttractionDetail detail;
        private JsonNode introInfo;

        public TouristAttractionDetailResponse(TouristAttractionDetail detail, JsonNode introInfo) {
            this.detail = detail;
            this.introInfo = introInfo;
        }

        public TouristAttractionDetail getDetail() {
            return detail;
        }

        public JsonNode getIntroInfo() {
            return introInfo;
        }
    }
    @GetMapping("/category/{category}")
    public ResponseEntity<List<TouristAttraction>> getTouristAttractionsByCategory(@PathVariable String category) {
        List<TouristAttraction> attractions = touristAttractionsService.getTouristAttractionsByCategory(category);
        return ResponseEntity.ok(attractions);

    }

    @GetMapping("/{contentid}/{contenttypeid}/intro")
    public ResponseEntity<?> getIntroInfo(@PathVariable String contentid, @PathVariable String contenttypeid) {
        JsonNode introInfo = touristAttractionsService.fetchIntroInfoFromApi(contentid, contenttypeid);
        if (introInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(introInfo);
    }
}
