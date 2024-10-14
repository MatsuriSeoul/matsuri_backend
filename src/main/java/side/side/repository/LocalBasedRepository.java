package side.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import side.side.model.LocalBase;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocalBasedRepository extends JpaRepository<LocalBase, Long> {

    @Modifying
    @Transactional
    @Query(value = "INSERT IGNORE INTO local_base (addr1, addr2, area_code, cat1, cat2, cat3, content_type_id, contentid, created_time, first_image, first_image2, mapx, mapy, modified_time, sigungu_code, telephone, title, zipcode) " +
            "VALUES (:#{#localBase.addr1}, :#{#localBase.addr2}, :#{#localBase.areaCode}, :#{#localBase.cat1}, :#{#localBase.cat2}, :#{#localBase.cat3}, :#{#localBase.contentTypeId}, :#{#localBase.contentid}, " +
            ":#{#localBase.createdTime}, :#{#localBase.firstImage}, :#{#localBase.firstImage2}, :#{#localBase.mapX}, :#{#localBase.mapY}, :#{#localBase.modifiedTime}, :#{#localBase.sigunguCode}, :#{#localBase.telephone}, " +
            ":#{#localBase.title}, :#{#localBase.zipcode})", nativeQuery = true)
    void insertIgnoreDuplicate(LocalBase localBase);

    // contentid로 중복된 데이터가 있는지 확인하는 메서드
    boolean existsByContentid(String contentid);

    // 추가된 메서드
    List<LocalBase> findByAreaCodeAndSigunguCode(int areaCode, int sigunguCode);

    @Query("SELECT DISTINCT l.sigunguCode FROM LocalBase l WHERE l.areaCode = :areaCode")
    List<Integer> findDistinctSigunguCodesByAreaCode(@Param("areaCode") int areaCode);

    // contenttypeid로 유사한 이벤트 조회
    List<LocalBase> findByContentTypeId(String contenttypeid);

    //여행톡
    Optional<LocalBase> findBycontentid(String contentid);
}
