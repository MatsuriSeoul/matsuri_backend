/**
    서울특별시 문화 행사 api
 */

package side.side.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "seoul_event")
public class SeoulEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gubun")
    private String gubun;

    @Column(name = "svcid")
    private String svcid;

    @Column(name = "maxclassnm")
    private String maxclassnm;

    @Column(name = "minclassnm")
    private String minclassnm;

    @Column(name = "svcstatnm")
    private String svcstatnm;

    @Column(name = "svcnm")
    private String svcnm;

    @Column(name = "payatnm")
    private String payatnm;

    @Column(name = "placenm")
    private String placenm;

    @Column(name = "usetgtinfo")
    private String usetgtinfo;

    @Column(name = "svcurl")
    private String svcurl;

    @Column(name = "x")
    private String x;

    @Column(name = "y")
    private String y;

    @Column(name = "svcopnbgndt")
    private String svcopnbgndt;

    @Column(name = "svcopnenddt")
    private String svcopnenddt;

    @Column(name = "rcptbgndt")
    private String rcptbgndt;

    @Column(name = "rcptenddt")
    private String rcptenddt;

    @Column(name = "areanm")
    private String areanm;

    @Column(name = "imgurl")
    private String imgurl;

    @Column(name = "dtlcont", columnDefinition = "LONGTEXT")
    private String dtlcont;

    @Column(name = "telno")
    private String telno;

    @Column(name = "v_min")
    private String vMin;

    @Column(name = "v_max")
    private String vMax;

    @Column(name = "revstddaynm")
    private String revstddaynm;

    @Column(name = "revstdday")
    private String revstdday;

    // 댓글
    @OneToMany(mappedBy = "svcid", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Comment> comments;
}
