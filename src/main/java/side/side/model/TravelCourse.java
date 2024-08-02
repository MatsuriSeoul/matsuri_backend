package side.side.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class TravelCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String addr1;
    private String overview;
    private String mapx;
    private String mapy;
    private String contentid;
    private String contenttypeid;
    private String areacode;
    private String sigungucode;
    private String zipcode;
    private String tel;
    private String modifiedtime;

    // Getter 및 Setter 메소드
}
