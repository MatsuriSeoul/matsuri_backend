package side.side.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "likes")
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserInfo user;

    @Column(name = "content_id", nullable = false)
    private String contentId; // 모델 Detail content id와 연결

    @Column(name = "content_type", nullable = false)
    private String contentType;  // 콘텐츠의 타입 (예: 문화시설, 관광지 등)

}
