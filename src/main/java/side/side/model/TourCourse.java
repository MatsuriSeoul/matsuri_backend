package side.side.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TourCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contentid;
    private String title;
    private String subname;
    private String subdetailoverview;
    private String subdetailimg;
    private String subdetailalt;
    private String distance;
    private String taketime;
    private String infocentertourcourse;
    private String schedule;
}
