package side.side;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;

@SpringBootApplication
@RequestMapping("/")
public class SideApplication {
    public static void main(String[] args) {
        SpringApplication.run(SideApplication.class, args);
    }
}
