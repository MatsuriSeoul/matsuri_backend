package side.side.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import side.side.service.LocalBasedService;

import java.util.Map;

@RestController
@RequestMapping("/api/region")
@CrossOrigin(origins = "http://localhost:8080")
public class LocalBasedController {

    @Autowired
    private LocalBasedService localBasedService;

    // 지역 및 시/군/구 관광 정보 조회 API
    @GetMapping("/{region}/{subregionCode}")
    public Map<String, Object> getLocalTourInfo(
            @PathVariable String region,
            @PathVariable int subregionCode,
            @RequestParam(defaultValue = "10") int numOfRows,
            @RequestParam(defaultValue = "1") int pageNo) {
        System.out.println("Region: " + region);
        System.out.println("Subregion Code: " + subregionCode);

        return localBasedService.getTourismInfo(region, subregionCode, numOfRows, pageNo);
    }
}
