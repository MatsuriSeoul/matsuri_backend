package side.side.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
    @Column(unique = true, nullable = false)
    private String contentid;
    private String contenttypeid;
    private String areacode;
    private String sigungucode;
    private String tel;
    private String overview;
    private String beginDe;
    private String endDe;
    private String regionNm;
    private String imageUrl;

    //   댓글
    @OneToMany(mappedBy = "contentid", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Comment> comments;

}
