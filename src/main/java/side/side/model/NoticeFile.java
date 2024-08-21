package side.side.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class NoticeFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String filePath;  // 파일 경로
    private String fileType;   // 파일 형식 (pdf, docx 등)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    @JsonBackReference
    private Notice notice;

}
