package side.side.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import side.side.config.CategoryMapping;
import side.side.model.TourEvent;
import side.side.repository.TourEventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class TourEventService {

    @Autowired
    private TourEventRepository tourEventRepository;

    private static final Logger logger = Logger.getLogger(TourEventService.class.getName());

    public List<TourEvent> getEventsByCategory(String categoryName) {
        String[] categoryCodes = CategoryMapping.getCategoryCodes(categoryName);
        if (categoryCodes == null) {
            logger.info("No category codes found for category: " + categoryName);
            return new ArrayList<>();
        }

        logger.info("Category Codes for " + categoryName + ": " + String.join(", ", categoryCodes));
        List<TourEvent> events = tourEventRepository.findByCat3In(categoryCodes);
        logger.info("Fetched Events: " + events);

        return events;
    }

    //  월 값에 맞는 행사 데이터 불러오기
    public List<TourEvent> getEventsByMonthAndRegion(String month, String region) {
        if (month.length()== 1) {
            month = "0" + month;
        } else if (month.equals("전체")) {
            month = "";
        }
        logger.info("Month: " + month + ", Region: " + region);
        return tourEventRepository.findByMonthAndRegion(month, region);
    }

    // contentid로 TourEvent 조회
    @Transactional
    public List<TourEvent> findBycontentid(String contentid) {
        return tourEventRepository.findBycontentid(contentid);
    }
}
