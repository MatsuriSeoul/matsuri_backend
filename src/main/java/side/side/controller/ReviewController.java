package side.side.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import side.side.service.NaverBlogService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);


    private final NaverBlogService naverBlogService;

    public ReviewController(NaverBlogService naverBlogService) {
        this.naverBlogService = naverBlogService;
    }

    @GetMapping
    public ResponseEntity<?> getReviewsByTitle(@RequestParam(value = "title", required = false) String title) {
        logger.info("Received request with title: {}", title);
        try {
            List<Map<String, String>> reviews = naverBlogService.getBlogReviews(title);
            if (reviews == null || reviews.isEmpty()) {
                return new ResponseEntity<>(reviews, HttpStatus.OK);
            }
            return new ResponseEntity<>(reviews, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error occurred while fetching reviews", e);
            return new ResponseEntity<>("리뷰 데이터를 불러오는 도중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
