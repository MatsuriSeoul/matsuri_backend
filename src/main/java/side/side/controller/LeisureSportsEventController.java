package side.side.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.model.FoodEvent;
import side.side.model.LeisureSportsEvent;
import side.side.model.LeisureSportsEventDetail;
import side.side.model.ShoppingEvent;
import side.side.repository.LeisureSportsEventRepository;
import side.side.service.LeisureSportsEventService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/leisure-sports")
public class LeisureSportsEventController {

    @Autowired
    private LeisureSportsEventService leisureSportsEventService;
    @Autowired
    private LeisureSportsEventRepository leisureSportsEventRepository;

    @GetMapping("/{contentid}/detail")
    public ResponseEntity<?> getLeisureSportsEventDetail(@PathVariable String contentid) {
        LeisureSportsEventDetail detail = leisureSportsEventService.getLeisureSportsEventDetailFromDB(contentid);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/{contentid}/{contenttypeid}/intro")
    public ResponseEntity<?> getIntroInfo(@PathVariable String contentid, @PathVariable String contenttypeid) {
        JsonNode introInfo = leisureSportsEventService.fetchIntroInfoFromApi(contentid, contenttypeid);
        if (introInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(introInfo);
    }
    // 이미지 정보 조회 불러오기 (외부 API에서)
    @GetMapping("/{contentid}/images")
    public ResponseEntity<?> getImages(@PathVariable String contentid) {
        JsonNode images = leisureSportsEventService.fetchImagesFromApi(contentid);
        if (images == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(images);
    }

    // 퍼스트 이미지 가져오기
    @GetMapping("/firstimage/{contentid}")
    public ResponseEntity<String> fetchFirstImage(@PathVariable String contentid) {
        Optional<LeisureSportsEvent> eventOptional = leisureSportsEventRepository.findByContentid(contentid);
        if (eventOptional.isPresent()) {
            LeisureSportsEvent event = eventOptional.get();
            return ResponseEntity.ok(event.getFirstimage());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // contentypeid가 28인 서비스 실행
    @GetMapping("/category/{category}")
    public ResponseEntity<List<LeisureSportsEvent>> getLeisureSportsEventsByCategory(@PathVariable String category) {
        List<LeisureSportsEvent> leisureSportsEvents = leisureSportsEventService.getLeisureSportsEventsByCategory(category);
        if (leisureSportsEvents.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(leisureSportsEvents);
    }
    // 키워드 추출
    @GetMapping("/by-region")
    public List<LeisureSportsEvent> getLeisureSportsByRegion(@RequestParam String region) {
        return leisureSportsEventService.getLeisureSportsByRegion(region);
    }
    // 유사한 레포츠 여행지 정보 가져오기
    @GetMapping("/{contenttypeid}/similar-events")
    public ResponseEntity<List<LeisureSportsEvent>> getSimilarLeisureSportsEvents(@PathVariable String contenttypeid) {
        List<LeisureSportsEvent> similarEvents = leisureSportsEventService.getSimilarLeisureSportsEvents(contenttypeid);
        if (similarEvents.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(similarEvents);
    }
}
