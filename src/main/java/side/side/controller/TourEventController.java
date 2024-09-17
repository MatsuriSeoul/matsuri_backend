package side.side.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import side.side.model.TourEvent;
import side.side.service.TourEventService;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/events")
public class TourEventController {

    @Autowired
    private TourEventService tourEventService;

    private static final Logger logger = Logger.getLogger(TourEventController.class.getName());

    @GetMapping("/category/{category}")
    public List<TourEvent> getEventsByCategory(@PathVariable String category) {
        List<TourEvent> events = tourEventService.getEventsByCategory(category);
        return events;

    }
}
