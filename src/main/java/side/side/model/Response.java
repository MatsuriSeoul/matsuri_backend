package side.side.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Response {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "inquiry_id")
    private Inquiry inquiry;

    private String username = "관리자";

    private String content;

    private LocalDateTime respondedTime;
}
