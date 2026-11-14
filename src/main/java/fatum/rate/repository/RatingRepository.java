package fatum.rate.repository;

import fatum.rate.model.Rating;
import fatum.rate.model.constant.RatingTarget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, String> {

    Optional<Rating> findByOrderIdAndTargetType(String orderId, RatingTarget targetType);

    List<Rating> findByOrderIdOrderByCreatedAtAsc(String orderId);

    List<Rating> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
            RatingTarget targetType, String targetId);
}
