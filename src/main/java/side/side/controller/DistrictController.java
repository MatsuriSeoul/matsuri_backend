package side.side.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import side.side.model.LocalBase;
import side.side.service.LocalBasedService;

import java.util.List;

@RestController
@RequestMapping("/api/district")
public class DistrictController {

    @Autowired
    private LocalBasedService localBasedService;

    // 상세 정보 가져오기
    @GetMapping("/{contentid}/detail")
    public ResponseEntity<?> getEventDetail(@PathVariable String contentid) {
        JsonNode detail = localBasedService.fetchEventDetail(contentid);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }

    // 소개 정보 가져오기
    @GetMapping("/{contentid}/{contenttypeid}/intro")
    public ResponseEntity<?> getIntroInfo(@PathVariable String contentid, @PathVariable String contenttypeid) {
        JsonNode introInfo = localBasedService.fetchIntroInfoFromApi(contentid, contenttypeid);

        if (introInfo == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(introInfo);
    }

    // 이미지 정보 가져오기
    @GetMapping("/{contentid}/images")
    public ResponseEntity<?> getImages(@PathVariable String contentid) {
        JsonNode images = localBasedService.fetchImagesFromApi(contentid);

        if (images == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(images);
    }
    // 유사한 여행지 정보 가져오기
    @GetMapping("/{contenttypeid}/similar-events")
    public ResponseEntity<List<LocalBase>> getSimilarEvents(@PathVariable String contenttypeid) {
        List<LocalBase> similarEvents = localBasedService.getSimilarEventsByContentType(contenttypeid);
        if (similarEvents.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(similarEvents);
    }

}
