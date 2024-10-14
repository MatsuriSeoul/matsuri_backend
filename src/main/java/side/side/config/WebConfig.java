package side.side.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 프로필 이미지 서빙
        registry.addResourceHandler("/uploads/userProfileImg/**")
                .addResourceLocations("file:" + System.getProperty("user.home") + "/Desktop/uploads/userProfileImg/");

        // 공지사항 이미지 서빙
        registry.addResourceHandler("/uploads/noticeImage/**")
                .addResourceLocations("file:" + System.getProperty("user.home") + "/Desktop/uploads/noticeImage/");

        //카테고리 댓글 이미지 서빙
        registry.addResourceHandler("/uploads/commentImage/**")
                .addResourceLocations("file:" + System.getProperty("user.home") + "/Desktop/uploads/commentImage/");
    }


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8080") // 허용할 클라이언트 도메인
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
