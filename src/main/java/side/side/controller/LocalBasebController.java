package side.side.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.model.LocalBase;
import side.side.service.LocalBasedService;

import java.util.List;


@RestController
@RequestMapping("/api/local")
@CrossOrigin(origins = "http://localhost:8080")
public class LocalBasebController {

    @Autowired
    private LocalBasedService localBasedService;

    @GetMapping("/{region}/{subregionCode}")
    public ResponseEntity<?> fetchAndSaveEvents(
            @PathVariable String region,
            @PathVariable int subregionCode,
            @RequestParam(defaultValue = "10") String numOfRows,
            @RequestParam(defaultValue = "1") String pageNo) {
        try {
            List<LocalBase> data = localBasedService.fetchAndSaveEvents(region, Integer.parseInt(numOfRows), Integer.parseInt(numOfRows), pageNo);
            return ResponseEntity.ok(data);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid region or subregion code.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching data.");
        }
    }
    // 지역과 시군구 코드로 이벤트를 조회하는 엔드포인트 추가
    @GetMapping("/events")
    public ResponseEntity<List<LocalBase>> getEventsByAreaAndSigungu(
            @RequestParam int areaCode,
            @RequestParam int sigunguCode) {
        List<LocalBase> events = localBasedService.getEventsByAreaAndSigungu(areaCode, sigunguCode);
        return ResponseEntity.ok(events);
    }
    // 지역 코드에 따른 시군구 목록을 반환하는 엔드포인트 추가
    @GetMapping("/districts")
    public ResponseEntity<List<Integer>> getSigunguCodesByAreaCode(@RequestParam int areaCode) {
        List<Integer> sigunguCodes = localBasedService.getSigunguCodesByAreaCode(areaCode);
        return ResponseEntity.ok(sigunguCodes);
    }
}
