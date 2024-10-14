package side.side.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "comment_image")
public class CommentImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imgName;

    private String imagePath;

    @ManyToOne
    @JoinColumn(name = "comment_id")
    @JsonBackReference
    private Comment comment;

    private String category;
    private String contentid;

    private String svcid;

    @ManyToOne
    @JoinColumn(name = "gyeonggi_event_id")
    private GyeonggiEvent gyeonggiEvent;
}

