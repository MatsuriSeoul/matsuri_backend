package side.side.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import side.side.model.CulturalFacility;
import side.side.model.TravelCourse;
import side.side.model.TravelCourseDetail;
import side.side.service.TravelCourseService;

import java.util.List;

@RestController
@RequestMapping("/api/travel-courses")
public class TravelCourseController {

    @Autowired
    private TravelCourseService travelCourseService;

    @GetMapping("/{contentid}/detail")
    public ResponseEntity<?> getTravelCourseDetail(@PathVariable String contentid) {
        TravelCourseDetail detail = travelCourseService.getTravelCourseDetailFromDB(contentid);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/{contentid}/{contenttypeid}/intro")
    public ResponseEntity<?> getIntroInfo(@PathVariable String contentid, @PathVariable String contenttypeid) {
        JsonNode introInfo = travelCourseService.fetchIntroInfoFromApi(contentid, contenttypeid);
        if (introInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(introInfo);
    }

    @GetMapping("/{contentid}/images")
    public ResponseEntity<?> getImages(@PathVariable String contentid) {
        JsonNode images = travelCourseService.fetchImagesFromApi(contentid);
        if (images == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(images);
    }
    @GetMapping("/category/{category}")
    public ResponseEntity<List<TravelCourse>> getTravelCoursesByCategory(@PathVariable String category) {
        List<TravelCourse> travelCourses = travelCourseService.getTravelCoursesByCategory(category);
        if (travelCourses.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(travelCourses);
    }
}
