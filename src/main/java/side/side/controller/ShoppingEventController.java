package side.side.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import side.side.model.ShoppingEvent;
import side.side.model.ShoppingEventDetail;
import side.side.service.ShoppingEventService;

import java.util.List;

@RestController
@RequestMapping("/api/shopping-events")
public class ShoppingEventController {

    @Autowired
    private ShoppingEventService shoppingEventService;

    // 쇼핑 이벤트 상세 정보 가져오기
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

    // 쇼핑 이벤트 카테고리별 리스트 가져오기
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ShoppingEvent>> getShoppingEventsByCategory(@PathVariable String category) {
        List<ShoppingEvent> shoppingEvents = shoppingEventService.getShoppingEventsByCategory(category);
        if (shoppingEvents.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(shoppingEvents);
    }
}
