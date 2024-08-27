package side.side.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_notice_view")
public class UserNoticeView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserInfo user;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "notice_id", referencedColumnName = "id")
    private Notice notice;

    @Column(name = "last_viewed")
    private LocalDate viewDate;

    public UserNoticeView() {}

    public UserNoticeView(UserInfo user, Notice notice, LocalDate viewDate) {
        this.user = user;
        this.notice = notice;
        this.viewDate = viewDate;
    }
}
