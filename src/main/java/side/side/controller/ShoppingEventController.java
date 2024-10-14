package side.side.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.model.CulturalFacilityDetail;
import side.side.model.FoodEvent;
import side.side.model.ShoppingEvent;
import side.side.model.ShoppingEventDetail;
import side.side.repository.ShoppingEventRepository;
import side.side.service.ShoppingEventService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/shopping-events")
public class ShoppingEventController {

    @Autowired
    private ShoppingEventService shoppingEventService;
    @Autowired
    private ShoppingEventRepository shoppingEventRepository;

    // 쇼핑 이벤트 상세 정보 가져오기 (Fallback 처리 추가)
    @GetMapping("/{contentid}/detail")
    public ResponseEntity<?> getShoppingEventDetail(@PathVariable String contentid) {
        ShoppingEventDetail detail = shoppingEventService.getShoppingEventDetailFromDB(contentid);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }

    // 쇼핑 소개 정보 가져오기
    @GetMapping("/{contentid}/{contenttypeid}/intro")
    public ResponseEntity<?> getIntroInfo(@PathVariable String contentid, @PathVariable String contenttypeid) {
        JsonNode introInfo = shoppingEventService.fetchIntroInfoFromApi(contentid, contenttypeid);
        if (introInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(introInfo);
    }

    // 쇼핑 이미지 정보 가져오기
    @GetMapping("/{contentid}/images")
    public ResponseEntity<?> getImages(@PathVariable String contentid) {
        JsonNode images = shoppingEventService.fetchImagesFromApi(contentid);
        if (images == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(images);
    }

    // 퍼스트 이미지 가져오기
    @GetMapping("/firstimage/{contentid}")
    public ResponseEntity<String> fetchFirstImage(@PathVariable String contentid) {
        Optional<ShoppingEvent> eventOptional = shoppingEventRepository.findByContentid(contentid);
        if (eventOptional.isPresent()) {
            ShoppingEvent event = eventOptional.get();
            return ResponseEntity.ok(event.getFirstimage());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 쇼핑 이벤트 카테고리별 리스트 가져오기
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ShoppingEvent>> getShoppingEventsByCategory(@PathVariable String category) {
        List<ShoppingEvent> shoppingEvents = shoppingEventService.getShoppingEventsByCategory(category);
        if (shoppingEvents.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(shoppingEvents);
    }
    // 키워드 추출
    @GetMapping("/by-region")
    public List<ShoppingEvent> getShoppingEventsByRegion(@RequestParam String region) {
        return shoppingEventService.getShoppingEventsByRegion(region);
    }
    // 유사한 쇼핑 여행지 정보 가져오기
    @GetMapping("/{contenttypeid}/similar-events")
    public ResponseEntity<List<ShoppingEvent>> getSimilarShoppingEvents(@PathVariable String contenttypeid) {
        List<ShoppingEvent> similarEvents = shoppingEventService.getSimilarShoppingEvents(contenttypeid);
        if (similarEvents.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(similarEvents);
    }
}
