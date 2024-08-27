package side.side.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import side.side.model.LeisureSportsEvent;
import side.side.model.LeisureSportsEventDetail;
import side.side.service.LeisureSportsEventService;

import java.util.List;

@RestController
@RequestMapping("/api/leisure-sports")
public class LeisureSportsEventController {

    @Autowired
    private LeisureSportsEventService leisureSportsEventService;

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

    @GetMapping("/{contentid}/images")
    public ResponseEntity<?> getImages(@PathVariable String contentid) {
        JsonNode images = leisureSportsEventService.fetchImagesFromApi(contentid);
        if (images == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(images);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<LeisureSportsEvent>> getLeisureSportsEventsByCategory(@PathVariable String category) {
        List<LeisureSportsEvent> leisureSportsEvents = leisureSportsEventService.getLeisureSportsEventsByCategory(category);
        if (leisureSportsEvents.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(leisureSportsEvents);
    }
}
