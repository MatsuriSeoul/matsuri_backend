/**
 경기도 문화 행사 현황 api
 */


package side.side.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "gyeonggi_event")
public class GyeonggiEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inst_nm", length = 255)
    private String instNm;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "category_nm", length = 255)
    private String categoryNm;

    @Column(name = "url", length = 255)
    private String url;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "begin_de", length = 20)
    private String beginDe;

    @Column(name = "end_de", length = 20)
    private String endDe;

    @Column(name = "addr", length = 255)
    private String addr;

    @Column(name = "event_tm_info", length = 255)
    private String eventTmInfo;

    @Column(name = "partcpt_expn_info", length = 255)
    private String partcptExpnInfo;

    @Column(name = "telno_info", length = 255)
    private String telnoInfo;

    @Column(name = "host_inst_nm", length = 255)
    private String hostInstNm;

    @Column(name = "hmpg_url", length = 255)
    private String hmpgUrl;

    @Column(name = "writng_de", length = 20)
    private String writngDe;
}
