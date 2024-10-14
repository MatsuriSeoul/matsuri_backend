package side.side.service;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class NaverBlogService {

    private static final Logger logger = LoggerFactory.getLogger(NaverBlogService.class);

    private final WebClient webClient;

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    @Value("${naver.api.uri}")
    private String apiUrl;

    public NaverBlogService(WebClient.Builder webClientBuilder) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(10))
                .wiretap(this.getClass().getCanonicalName(),
                        io.netty.handler.logging.LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);

        this.webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    public List<Map<String, String>> getBlogReviews(String query) {
        List<Map<String, String>> blogPosts = new ArrayList<>();
        try {

            String searchQuery = query + " 후기";
            String uri = UriComponentsBuilder.fromHttpUrl(apiUrl)
                    .queryParam("query", searchQuery)  // 인코딩되지 않은 쿼리 사용
                    .queryParam("display", 10) // 원하는 결과 개수 제한
                    .queryParam("sort", "sim") // 관련도 순 정렬
                    .toUriString();

            String decodedUri = URLDecoder.decode(uri, StandardCharsets.UTF_8.toString());
            logger.info("Requesting Naver Blog API with decoded URL: {}", decodedUri);
            logger.info("Requesting with original query: {}", query);

            Mono<String> responseMono = webClient.get()
                    .uri(decodedUri)
                    .headers(headers -> {
                        headers.set("X-Naver-Client-Id", clientId);
                        headers.set("X-Naver-Client-Secret", clientSecret);
                        headers.set("Accept-Charset", "UTF-8"); // 응답 인코딩 요청
                        headers.set("User-Agent", "Mozilla/5.0"); // 필요시 추가
                        headers.set("Accept", "application/json");
                    })
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        logger.error("네이버 블로그 API 요청 실패: 상태 코드 - {}", clientResponse.statusCode());
                        return Mono.error(new RuntimeException("네이버 블로그 API 요청 실패: " + clientResponse.statusCode()));
                    })
                    .bodyToMono(String.class);

            String response = responseMono.block();
            logger.info("네이버 API 응답 본문: {}", response); // 응답 본문 로그 추가

            if (response != null) {
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray items = jsonResponse.getJSONArray("items");

                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    String titleWithHtml = item.getString("title");
                    String titleDecoded = URLDecoder.decode(StringEscapeUtils.unescapeHtml4(titleWithHtml), StandardCharsets.UTF_8.toString());
                    String titleWithoutHtml = titleDecoded.replaceAll("<.*?>", ""); // HTML 태그 제거

                    String descriptionWithHtml = item.getString("description");
                    String descriptionDecoded = URLDecoder.decode(StringEscapeUtils.unescapeHtml4(descriptionWithHtml), StandardCharsets.UTF_8.toString());
                    String descriptionWithoutHtml = descriptionDecoded.replaceAll("<.*?>", ""); // HTML 태그 제거

                    String link = item.getString("link");

                    // 디코딩된 title과 description을 통해 원하는 데이터를 필터링
                    if (titleWithoutHtml.toLowerCase().contains(query.toLowerCase())) {
                        blogPosts.add(Map.of(
                                "title", titleWithoutHtml,
                                "link", link,
                                "description", descriptionWithoutHtml
                        ));
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            logger.error("URL 디코딩 중 오류 발생: 잘못된 인코딩 형식", e);
        } catch (Exception e) {
            logger.error("네이버 블로그 API 호출 중 오류 발생", e);
        }

        return blogPosts;
    }
}