package side.side.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "user_detail_view")
public class UserDetailView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String contenttypeid;

    @Column(nullable = false)
    private String contentid;

    @Column
    private int viewCnt;

    @Column(nullable = false)
    private LocalDate viewDate;  // 조회한 날짜
}
