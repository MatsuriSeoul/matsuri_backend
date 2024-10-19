package side.side.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UserClickLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contentid; // 사용자가 클릭한 콘텐츠 ID
    private String contenttypeid; // 해당 콘텐츠의 카테고리 타입 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserInfo user; // 사용자 정보
}
