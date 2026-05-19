// Repository Template - JPA Repository (Java 8)

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SampleRepository extends JpaRepository<SampleEntity, String> {

    Optional<SampleEntity> findByName(String name);

    List<SampleEntity> findByStatus(String status);

    boolean existsByName(String name);

    @Query("SELECT e FROM SampleEntity e WHERE e.name LIKE %:keyword%")
    List<SampleEntity> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT e FROM SampleEntity e JOIN FETCH e.relatedEntity WHERE e.id = :id")
    Optional<SampleEntity> findWithRelatedEntity(@Param("id") String id);
}
