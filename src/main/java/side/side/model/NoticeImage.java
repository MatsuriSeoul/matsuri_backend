package side.side.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "notice_image")
public class NoticeImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imgName;

    private String imagePath;

    @ManyToOne
    @JoinColumn(name = "notice_id")
    @JsonBackReference
    private Notice notice;
}
