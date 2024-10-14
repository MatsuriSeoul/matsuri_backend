package side.side.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.model.*;
import side.side.repository.TravelCourseRepository;
import side.side.service.TravelCourseService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/travel-courses")
public class TravelCourseController {

    @Autowired
    private TravelCourseService travelCourseService;
    @Autowired
    private TravelCourseRepository travelCourseRepository;

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

    // 퍼스트 이미지 가져오기
    @GetMapping("/firstimage/{contentid}")
    public ResponseEntity<String> fetchFirstImage(@PathVariable String contentid) {
        Optional<TravelCourse> eventOptional = travelCourseRepository.findByContentid(contentid);
        if (eventOptional.isPresent()) {
            TravelCourse event = eventOptional.get();
            return ResponseEntity.ok(event.getImageUrl());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<TravelCourse>> getTravelCoursesByCategory(@PathVariable String category) {
        List<TravelCourse> travelCourses = travelCourseService.getTravelCoursesByCategory(category);
        if (travelCourses.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(travelCourses);
    }
    // 키워드 추출
    @GetMapping("/by-region")
    public List<TravelCourse> getTravelCourseByRegion(@RequestParam String region) {
        return travelCourseService.getTravelCourseByRegion(region);
    }
    // 유사한 여행 코스 정보 가져오기
    @GetMapping("/{contenttypeid}/similar-events")
    public ResponseEntity<List<TravelCourse>> getSimilarTravelCourses(@PathVariable String contenttypeid) {
        List<TravelCourse> similarEvents = travelCourseService.getSimilarTravelCourses(contenttypeid);
        if (similarEvents.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(similarEvents);
    }
}
