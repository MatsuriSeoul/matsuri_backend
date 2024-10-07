package side.side.model.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventDTO {
    private String contentid;
    private String contenttypeid;
    private String title;
    private String firstImage;
    private String imgurl;
    private String imageUrl;
    private String overview;
    // 서울과 경기 이벤트의 경우
    private String svcid;  // 서울 이벤트 ID
    private Long id;       // 경기 이벤트 ID


    public EventDTO(String contentid, String contenttypeid, String title, String firstImage, String imgurl, String imageUrl, String overview, String svcid, Long id) {
        this.contentid = contentid;
        this.contenttypeid = contenttypeid;
        this.title = title;
        this.firstImage = firstImage;
        this.imgurl = imgurl;
        this.imageUrl = imageUrl;
        this.overview = overview;
        this.svcid = svcid;
        this.id = id;
    }
}
