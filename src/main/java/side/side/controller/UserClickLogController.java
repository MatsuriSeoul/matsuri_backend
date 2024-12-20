package side.side.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.side.config.JwtUtils;
import side.side.service.UserClickLogService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/clicks")
public class UserClickLogController {

    @Autowired
    private UserClickLogService userClickLogService;

    @Autowired
    private JwtUtils jwtUtils;


    @PostMapping("/log")
    public ResponseEntity<String> logUserClick(
            @RequestHeader("Authorization") String token, // JWT 토큰을 요청 헤더에서 받음
            @RequestBody ClickLogRequest request) {

        // JWT 토큰에서 사용자 정보 추출
        Long userId = jwtUtils.extractUserId(token);  // JWT에서 userId 추출
        if (userId == null) {
            return ResponseEntity.status(401).body("토큰이 유효하지 않아요");
        }

        // 클릭 로그 저장
        userClickLogService.logUserClick(userId, request.getContentid(), request.getCategory());

        return ResponseEntity.ok("로그를 남기는데 성공함");
    }
    // 사용자별 추천 데이터를 제공
    @GetMapping("/personalized/recommendation")
    public ResponseEntity<Map<String, Object>> getPersonalizedRecommendation(
            @RequestHeader("Authorization") String token) {

        Long userId = jwtUtils.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "유효하지 않은 토큰입니다."));
        }

        // 사용자가 가장 많이 조회한 contenttypeid 가져오기
        String mostViewedCategory;
        try {
            mostViewedCategory = userClickLogService.findMostViewedCategoryByUser(userId);
        } catch (IndexOutOfBoundsException e) {
            return ResponseEntity.ok(Map.of("message", "사용자의 클릭 로그가 없습니다."));
        }

        // 가장 많이 조회한 카테고리에 맞는 데이터를 반환
        if (mostViewedCategory != null) {
            Map<String, String> categoryMapping = Map.of(
                    "12", "관광지",
                    "14", "문화시설",
                    "15", "행사",
                    "25", "여행코스",
                    "28", "레포츠",
                    "32", "숙박",
                    "38", "쇼핑",
                    "39", "음식"
            );

            String categoryName = categoryMapping.get(mostViewedCategory);

            // 해당 카테고리의 데이터를 가져오기
            List<Map<String, Object>> categoryData;
            try {
                categoryData = userClickLogService.getCategoryData(mostViewedCategory);
            } catch (Exception e) {
                return ResponseEntity.ok(Map.of("message", "해당 카테고리에 대한 데이터를 찾을 수 없습니다."));
            }

            // 데이터가 없는 경우 처리
            if (categoryData == null || categoryData.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "추천할 데이터가 없습니다."));
            }

            // 갯수 제한 없이 무작위로 섞기
            Collections.shuffle(categoryData); // 전체 데이터를 무작위로 섞음

            return ResponseEntity.ok(Map.of(
                    "categoryName", categoryName,
                    "categoryData", categoryData
            ));
        } else {
            return ResponseEntity.ok(Map.of("message", "추천할 카테고리가 없습니다."));
        }
    }


    @GetMapping("/category-data")
    public ResponseEntity<List<Map<String, Object>>> getCategoryData(
            @RequestHeader("Authorization") String token) {

        Long userId = jwtUtils.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(Collections.emptyList());
        }

        // 사용자가 가장 많이 조회한 contenttypeid 가져오기
        String mostViewedCategory = userClickLogService.findMostViewedCategoryByUser(userId);

        // 카테고리에 맞는 데이터를 반환
        if (mostViewedCategory != null) {
            List<Map<String, Object>> categoryData = userClickLogService.getCategoryData(mostViewedCategory);
            return ResponseEntity.ok(categoryData);
        } else {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    // 모든 사용자들의 선호 데이터를 기반으로 인기 있는 콘텐츠를 반환
    @GetMapping("/popular")
    public ResponseEntity<List<Map<String, Object>>> getPopularContent() {
        List<Map<String, Object>> popularContent = userClickLogService.findTopContentByAllUsers();
        return ResponseEntity.ok(popularContent);
    }

    @Getter
    @Setter
    public static class ClickLogRequest {
        private String contentid;  // 콘텐츠 ID
        private String category;   // 카테고리 이름

    }
}
