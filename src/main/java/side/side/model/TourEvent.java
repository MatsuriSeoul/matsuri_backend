/**
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

    private String title;
    private String addr1;
    private String eventstartdate;
    private String eventenddate;
    private String firstimage;
}
