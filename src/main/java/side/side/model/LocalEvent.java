package side.side.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class LocalEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String addr1;
    private String eventstartdate;
    private String eventenddate;
    private String firstimage;
    private String cat1;
    private String cat2;
    private String cat3;
    private String contentid;
    private String contenttypeid;

}
