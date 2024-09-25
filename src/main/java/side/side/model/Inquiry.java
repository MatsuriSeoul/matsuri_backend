package side.side.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonManagedReference  // Inquiry가 직렬화될 때 InquiryResponse도 포함
    private InquiryResponse response;

    private String status;  // 답변 상태 추가 (예: "답변 대기 중", "답변 완료")

    @JsonProperty("isPublic") // JSON 직렬화 및 역직렬화 시 필드 이름을 명확히 지정
    private boolean isPublic;
}
