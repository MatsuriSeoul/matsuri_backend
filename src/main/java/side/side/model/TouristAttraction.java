package side.side.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class TouristAttraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;        // 제목
    private String addr1;        // 주소
    private String firstimage;   // 대표 이미지 URL
    private String mapx;         // GPS X좌표
    private String mapy;         // GPS Y좌표
    private String overview;     // 개요
    private String contenttypeid; // 콘텐츠 타입 ID (여기서는 항상 12)
    private String areacode;     // 지역 코드
    private String sigungucode;  // 시군구 코드
    private String zipcode;      // 우편번호
    private String tel;          // 전화번호
    private String modifiedtime; // 수정일
    private String beginDe;
    private String endDe;
    private String regionNm;
    private String imageUrl;

    private String contentid;      //콘텐츠 ID

    //   댓글
    @OneToMany(mappedBy = "contentid", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Comment> comments;

}
