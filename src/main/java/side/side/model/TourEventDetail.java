package side.side.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TourEventDetail {

    @Id
    private String contentid;

    private String contenttypeid;
    private String booktour;
    private String createdtime;
    private String homepage;
    private String modifiedtime;
    private String tel;
    private String telname;
    private String title;
    private String firstimage;
    private String firstimage2;
    private String cpyrhtDivCd;
    private String areacode;
    private String sigungucode;
    private String cat1;
    private String cat2;
    private String cat3;
    private String addr1;
    private String addr2;
    private String zipcode;
    private String mapx;
    private String mapy;
    private String mlevel;

    @Column(columnDefinition = "TEXT")
    private String overview;

}
