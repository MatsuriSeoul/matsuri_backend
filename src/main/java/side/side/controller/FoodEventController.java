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
import java.util.Optional;

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

    // 퍼스트 이미지 가져오기
    @GetMapping("/firstimage/{contentid}")
    public ResponseEntity<String> fetchFirstImage(@PathVariable String contentid) {
        Optional<FoodEvent> eventOptional = foodEventRepository.findByContentid(contentid);
        if (eventOptional.isPresent()) {
            FoodEvent event = eventOptional.get();
            return ResponseEntity.ok(event.getFirstimage());
        } else {
            return ResponseEntity.notFound().build();
        }
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
    // 유사한 음식 여행지 정보 가져오기
    @GetMapping("/{contenttypeid}/similar-events")
    public ResponseEntity<List<FoodEvent>> getSimilarFoodEvents(@PathVariable String contenttypeid) {
        List<FoodEvent> similarEvents = foodEventService.getSimilarFoodEvents(contenttypeid);
        if (similarEvents.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(similarEvents);
    }

}
