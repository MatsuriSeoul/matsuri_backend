package side.side.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
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

    private String svcid;

    private String category;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserInfo author;  // 작성자 정보

    private String maskedAuthor;  //  마스킹된 작성자 이름

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<CommentImage> images = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt; // 댓글 작성 날짜

    @ManyToOne
    @JoinColumn(name = "svcid", referencedColumnName = "svcid", insertable = false, updatable = false)
    private SeoulEvent seoulEvent;

    @ManyToOne
    @JoinColumn(name = "gyeonggi_event_id")
    private GyeonggiEvent gyeonggiEvent;

    // 연관 관계 편의 메서드
    public void addImage(CommentImage image) {
        images.add(image);
        image.setComment(this);
    }

    public void removeImage(CommentImage image) {
        images.remove(image);
        image.setComment(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return id != null && id.equals(comment.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
