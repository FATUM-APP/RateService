package fatum.rate.service;

import fatum.rate.dto.response.ServiceScoreResponse;
import fatum.rate.model.constant.RatingTarget;
import fatum.rate.repository.OrderSnapshotRepository;
import fatum.rate.repository.RatingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Averages read on demand.
 *
 * <p>A user keeps its running sum because it moves with every rating. A service does not: its
 * ratings are never updated, so the average is one aggregate over an indexed column and there is
 * no copy that could drift.</p>
 */
@Service
public class ScoreService {

    /** What a service answers while no order of it has been completed. */
    private static final BigDecimal SEED_SCORE = new BigDecimal("3.00");

    private final RatingRepository ratingRepository;
    private final OrderSnapshotRepository orderRepository;

    public ScoreService(RatingRepository ratingRepository, OrderSnapshotRepository orderRepository) {
        this.ratingRepository = ratingRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public ServiceScoreResponse serviceScore(String serviceId) {
        String id = serviceId.trim();
        if (!orderRepository.existsByServiceIdAndCompletedTrue(id)) {
            return new ServiceScoreResponse(id, SEED_SCORE, 0, true);
        }
        long count = ratingRepository.countByTargetTypeAndTargetId(RatingTarget.SERVICE, id);
        Double average = ratingRepository.averageByTargetTypeAndTargetId(RatingTarget.SERVICE, id);
        if (count == 0 || average == null) {
            return new ServiceScoreResponse(id, SEED_SCORE, 0, true);
        }
        return new ServiceScoreResponse(
                id,
                BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP),
                (int) count,
                false);
    }
}
