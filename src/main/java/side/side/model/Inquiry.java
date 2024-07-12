package side.side.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Inquiry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    private Long userId;

    private LocalDateTime createdTime;

    @OneToOne(mappedBy = "inquiry", cascade = CascadeType.ALL)
    private Response response;
}
