/**
 *  한국관광공사_국문 관광정보 서비스_GW API
 * API 목록에서 파라미터 받아오려면 제공하는 파라미터 변수명 확인 후 모델 선언 해줘야함
 */



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
public class TourEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // 제목
    private String addr1; // 주소 1
    private String eventstartdate; // 시작일
    private String eventenddate; // 마감일
    private String firstimage; // 대표이미지
    private String tel;  //전화번호
    private String cat1; // 대분류
    private String cat2; // 중분류
    private String cat3; // 소분류
    private String contentid; // 콘텐츠ID
    private String contenttypeid; // 관광타입(관광지, 숙박등) ID

}
