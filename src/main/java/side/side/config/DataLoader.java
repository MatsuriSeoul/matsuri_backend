package side.side.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import side.side.service.EventService;

@Component
public class DataLoader implements ApplicationRunner {

    @Autowired
    private EventService eventService;

    @Override
    public void run(ApplicationArguments args) {
        eventService.fetchAndSaveGyeonggiEvents();
        eventService.fetchAndSaveSeoulEvents();
    }
}
