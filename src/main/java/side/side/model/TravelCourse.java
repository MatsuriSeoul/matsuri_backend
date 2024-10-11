package side.side.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
    private String contenttypeid;
    private String areacode;
    private String sigungucode;
    private String zipcode;
    private String tel;
    private String modifiedtime;
    private String beginDe;
    private String endDe;
    private String regionNm;
    private String imageUrl;

    private String contentid;

    //   댓글
    @OneToMany(mappedBy = "contentid", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Comment> comments;
}
