package side.side.model.DTO;

import lombok.Getter;
import lombok.Setter;
import side.side.model.Comment;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentDTO {
    private Long id;                //경기도 이벤트
    private String content;
    private String contenttypeid;  // contenttypeid 필드 추가
    private String contentid;       // contentid 필드 추가
    private String svcid;           // svcid 필드 추가 (서울 이벤트)
    private LocalDateTime createdAt;
    private String maskedAuthor;

    // 생성자
    public CommentDTO(Comment comment, String contenttypeid, String contentid, String svcid, Long id) {
        this.content = comment.getContent();
        this.contenttypeid = contenttypeid;
        this.contentid = contentid;
        this.svcid = svcid;
        this.id = id;
        this.createdAt = comment.getCreatedAt();
        this.maskedAuthor = comment.getMaskedAuthor();

    }

}