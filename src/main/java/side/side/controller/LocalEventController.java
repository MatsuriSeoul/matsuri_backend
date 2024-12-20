package side.side.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.model.FoodEvent;
import side.side.model.LocalEvent;
import side.side.model.LocalEventDetail;
import side.side.model.TouristAttraction;
import side.side.repository.LocalEventRepository;
import side.side.service.LocalEventService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/local-events")
public class LocalEventController {

    @Autowired
    private LocalEventService localEventService;
    @Autowired
    private LocalEventRepository localEventRepository;

    @GetMapping("/{contentid}/detail")
    public ResponseEntity<?> getLocalEventDetail(@PathVariable String contentid) {
        LocalEventDetail detail = localEventService.getAccommodationDetailFromDB(contentid);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/{contentid}/{contenttypeid}/intro")
    public ResponseEntity<?> getIntroInfo(@PathVariable String contentid, @PathVariable String contenttypeid) {
        JsonNode introInfo = localEventService.fetchIntroInfoFromApi(contentid, contenttypeid);
        if (introInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(introInfo);
    }

    @GetMapping("/{contentid}/images")
    public ResponseEntity<?> getImages(@PathVariable String contentid) {
        JsonNode images = localEventService.fetchImagesFromApi(contentid);
        if (images == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(images);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<LocalEvent>> getLocalEventsByCategory(@PathVariable String category) {
        List<LocalEvent> localEvents = localEventService.getAccommodationsByCategory(category);
        if (localEvents.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(localEvents);
    }
    // 키워드 추출
    @GetMapping("/by-region")
    public List<LocalEvent> getLocalEventsByRegion(@RequestParam String region) {
        return localEventService.getLocalEventsByRegion(region);
    }
    // 유사한 숙박 여행지 정보 가져오기
    @GetMapping("/{contenttypeid}/similar-events")
    public ResponseEntity<List<LocalEvent>> getSimilarLocalEvents(@PathVariable String contenttypeid) {
        List<LocalEvent> similarEvents = localEventService.getSimilarLocalEvents(contenttypeid);
        if (similarEvents.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(similarEvents);
    }

    // 퍼스트 이미지 가져오기
    @GetMapping("/firstimage/{contentid}")
    public ResponseEntity<String> fetchFirstImage(@PathVariable String contentid) {
        Optional<LocalEvent> eventOptional = localEventRepository.findByContentid(contentid);
        if (eventOptional.isPresent()) {
            LocalEvent event = eventOptional.get();
            return ResponseEntity.ok(event.getFirstimage());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
