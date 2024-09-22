package side.side.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InquiryDTO {
    private String title;
    private String content;
    private boolean isPublic;  // 공개 여부
}
