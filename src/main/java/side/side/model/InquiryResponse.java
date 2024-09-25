package side.side.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class InquiryResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "inquiry_id")
    @JsonBackReference  // 순환 참조 방지: InquiryResponse 직렬화 시 Inquiry는 포함되지 않음
    private Inquiry inquiry;

    private String username = "관리자";

    private String content;

    private LocalDateTime respondedTime;
}
