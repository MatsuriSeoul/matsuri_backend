package side.side.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import side.side.model.FoodEvent;
import side.side.model.FoodEventDetail;
import side.side.service.FoodEventService;

import java.util.List;

@RestController
@RequestMapping("/api/food-events")
public class FoodEventController {

    @Autowired
    private FoodEventService foodEventService;

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
}
