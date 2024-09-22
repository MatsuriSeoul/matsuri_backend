package side.side.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.model.FoodEvent;
import side.side.model.FoodEventDetail;
import side.side.model.ShoppingEvent;
import side.side.repository.FoodEventRepository;
import side.side.service.FoodEventService;

import java.util.List;

@RestController
@RequestMapping("/api/food-events")
public class FoodEventController {

    @Autowired
    private FoodEventService foodEventService;
    @Autowired
    private FoodEventRepository foodEventRepository;

    @GetMapping("/{contentid}/detail")
    public ResponseEntity<?> getFoodEventDetail(@PathVariable String contentid) {
        FoodEventDetail detail = foodEventService.getFoodEventDetailFromDB(contentid);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/{contentid}/{contenttypeid}/intro")
    public ResponseEntity<?> getIntroInfo(@PathVariable String contentid, @PathVariable String contenttypeid) {
        JsonNode introInfo = foodEventService.fetchIntroInfoFromApi(contentid, contenttypeid);
        if (introInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(introInfo);
    }

    @GetMapping("/{contentid}/images")
    public ResponseEntity<?> getImages(@PathVariable String contentid) {
        JsonNode images = foodEventService.fetchImagesFromApi(contentid);
        if (images == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(images);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<FoodEvent>> getFoodEventsByCategory(@PathVariable String category) {
        List<FoodEvent> foodEvents = foodEventService.getFoodEventsByCategory(category);
        if (foodEvents.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(foodEvents);
    }
    // 키워드 추출
    @GetMapping("/by-region")
    public List<FoodEvent> getFoodEventsByRegion(@RequestParam String region) {
        return foodEventService.getFoodEventsByRegion(region);
    }
}
