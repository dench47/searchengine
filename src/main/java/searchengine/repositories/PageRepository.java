package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.model.Status;
import searchengine.model.WebSite;

import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {

    @Query(value = "SELECT CASE WHEN count(*) <> 0 = 1 THEN 'true' ELSE 'false' END FROM page where site_id = :id AND path LIKE %:path%", nativeQuery = true)
    boolean existsBySiteIdAndPath(Integer id, String path);

//    @Query(value = "SELECT count(*) <> 0 FROM site where id = :id And status = FAILED", nativeQuery = true)
//    int existByIdAndStatus(Integer id, Status status);
}
