package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;
import searchengine.model.Status;
import searchengine.model.WebSite;

import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {

    @Query(value = "SELECT CASE WHEN count(*) <> 0 = 1 THEN 'true' ELSE 'false' END FROM page where site_id = :id AND path LIKE %:path%", nativeQuery = true)
    boolean existsBySiteIdAndPath(Integer id, String path);

    @Transactional
    @Modifying
    @Query(value = "DELETE from page WHERE site_id = :id", nativeQuery = true)
    void deleteAllPagesOfSite(Integer id);


}
