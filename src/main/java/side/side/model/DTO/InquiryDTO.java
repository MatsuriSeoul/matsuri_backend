package side.side.model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InquiryDTO {
    private String title;
    private String content;

    @JsonProperty("isPublic") // JSON 직렬화 및 역직렬화 시 필드 이름을 명확히 지정
    private boolean isPublic;  // 공개 여부
}
