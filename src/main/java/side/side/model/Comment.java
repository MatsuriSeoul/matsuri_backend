package side.side.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "notice_id")
    private Notice notice;

    private String contentid;

    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserInfo author;  // 작성자 정보

    private String maskedAuthor;  //  마스킹된 작성자 이름

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<CommentImage> images = new ArrayList<>();

}
