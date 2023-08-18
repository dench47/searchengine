package searchengine.repositories;

import org.hibernate.annotations.SQLUpdate;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.WebSite;

import java.time.LocalDateTime;

@Repository
public interface WebSiteRepository extends CrudRepository<WebSite, Integer> {
    @Transactional
    @Modifying
    @Query(value = "UPDATE site SET status_time = NOW() WHERE id = :id", nativeQuery = true)
void changeStatusTime(Integer id);

}
