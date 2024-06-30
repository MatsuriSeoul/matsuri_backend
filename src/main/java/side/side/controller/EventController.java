package side.side.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import side.side.service.EventService;

@RestController
@RequestMapping("/")
public class EventController {

    @Autowired
    private EventService eventService;

    @GetMapping("/fetchGyeonggi")
    public String fetchGyeonggiEvents() {
        eventService.fetchAndSaveGyeonggiEvents();
        return "경기도 api 저장 완료";
    }

    @GetMapping("/fetchSeoul")
    public String fetchSeoulEvents() {
        eventService.fetchAndSaveSeoulEvents();
        return "서울 api 저장 완료";
    }
}
