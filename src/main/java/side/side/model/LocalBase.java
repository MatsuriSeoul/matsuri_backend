package side.side.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class LocalBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column
    private String addr1;

    @Column
    private String addr2;

    @Column
    private String firstImage;

    @Column
    private String firstImage2;

    @Column(nullable = false)
    private int areaCode;

    @Column
    private int sigunguCode;

    @Column
    private String cat1;

    @Column
    private String cat2;

    @Column
    private String cat3;

    @Column
    private String contentTypeId;

    @Column(nullable = false, unique = true)
    private String contentid; // API에서 제공하는 고유 식별자

    @Column
    private LocalDateTime createdTime;

    @Column
    private LocalDateTime modifiedTime;

    @Column
    private double mapX;

    @Column
    private double mapY;

    @Column
    private String telephone;

    @Column
    private String zipcode;


}
