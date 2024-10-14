package side.side.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class LocalEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String addr1;
    private String eventstartdate;
    private String eventenddate;
    private String firstimage;
    private String cat1;
    private String cat2;
    private String cat3;
    private String contenttypeid;
    private String beginDe;
    private String endDe;
    private String regionNm;
    private String imageUrl;

    private String contentid;

    private String mapx;
    private String mapy;
    //   댓글
    @OneToMany(mappedBy = "contentid", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Comment> comments;
}
