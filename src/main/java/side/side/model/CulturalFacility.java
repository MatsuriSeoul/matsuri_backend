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
public class CulturalFacility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String addr1;
    private String firstimage;
    private String mapx;
    private String mapy;
    private String contentid;
    private String contenttypeid;
    private String areacode;
    private String sigungucode;
    private String tel;
    private String overview;


}
