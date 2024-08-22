package side.side.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserInfo author;  // 작성자 정보

    private String maskedAuthor;  //  마스킹된 작성자 이름

}
