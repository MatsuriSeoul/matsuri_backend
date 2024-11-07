package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import side.side.model.UserDetailView;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface UserDetailViewRepository extends JpaRepository<UserDetailView, Long> {

    @Query("SELECT v FROM UserDetailView v WHERE v.userId = :userId AND v.contenttypeid = :contenttypeid AND v.contentid = :contentid AND v.viewDate = :viewDate")
    Optional<UserDetailView> findViewByUserAndContentAndDate(@Param("userId") Long userId,
                                                             @Param("contenttypeid") String contenttypeid,
                                                             @Param("contentid") String contentid,
                                                             @Param("viewDate") LocalDate viewDate);

    @Query("SELECT COUNT(v) FROM UserDetailView v WHERE v.contenttypeid = :contenttypeid AND v.contentid = :contentid")
    int countByContenttypeidAndContentid(@Param("contenttypeid") String contenttypeid, @Param("contentid") String contentid);
}


