package fatum.rate.repository;

import fatum.rate.model.Rating;
import fatum.rate.model.constant.RatingTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, String> {

    Optional<Rating> findByOrderIdAndTargetType(String orderId, RatingTarget targetType);

    List<Rating> findByOrderIdOrderByCreatedAtAsc(String orderId);

    List<Rating> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
            RatingTarget targetType, String targetId);

    long countByTargetTypeAndTargetId(RatingTarget targetType, String targetId);

    @Query("""
            select avg(r.score)
            from Rating r
            where r.targetType = :targetType and r.targetId = :targetId
            """)
    Double averageByTargetTypeAndTargetId(
            @Param("targetType") RatingTarget targetType,
            @Param("targetId") String targetId);

}
