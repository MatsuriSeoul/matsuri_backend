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
            List<LocalBase> data = localBasedService.fetchAndSaveEvents(region, Integer.parseInt(numOfRows), pageNo);
            return ResponseEntity.ok(data);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid region or subregion code.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching data.");
        }
    }
}
