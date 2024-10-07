package side.side.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class TourEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // 제목
    private String addr1; // 주소 1
    private String eventstartdate; // 시작일
    private String eventenddate; // 마감일
    private String firstimage; // 대표이미지
    private String tel;  // 전화번호
    private String cat1; // 대분류
    private String cat2; // 중분류
    private String cat3; // 소분류

    @Column(unique = true, nullable = false)
    private String contentid; // 콘텐츠ID

    private String contenttypeid; // 관광타입(관광지, 숙박등) ID
    private String beginDe;
    private String endDe;
    private String regionNm;
    private String imageUrl;

    //   댓글
    @OneToMany(mappedBy = "contentid", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Comment> comments;
}
