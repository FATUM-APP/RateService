package fatum.rate.service;

import fatum.rate.dto.mapper.RatingMapper;
import fatum.rate.dto.request.RatingRequest;
import fatum.rate.dto.response.RatingResponse;
import fatum.rate.exception.CalificationErrorCode;
import fatum.rate.exception.FatumServiceException;
import fatum.rate.model.OrderSnapshot;
import fatum.rate.model.Rating;
import fatum.rate.model.UserSnapshot;
import fatum.rate.model.constant.RatingTarget;
import fatum.rate.repository.RatingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final SnapshotService snapshotService;

    public RatingService(RatingRepository ratingRepository, SnapshotService snapshotService) {
        this.ratingRepository = ratingRepository;
        this.snapshotService = snapshotService;
    }

    /**
     * Writes the rating the user sent over the one written at completion.
     *
     * <p>The order already has its three ratings, so this is normally an update. It is allowed
     * exactly once per rating: the first submission applies the score and flips {@code updated};
     * a second one is refused. If the row is not there — an order completed before this rule
     * existed — it is created directly as the user's rating.</p>
     *
     * <p>The only checks are that the order exists as completed. No identity, role, active state,
     * verification or party of the order is checked.</p>
     */
    @Transactional
    public RatingResponse register(RatingRequest request) {
        OrderSnapshot order = snapshotService.requireOrder(request.getOrderId().trim());
        if (!order.canBeRated()) {
            throw new FatumServiceException(CalificationErrorCode.ORDER_NOT_COMPLETED);
        }

        RatingTarget target = request.getTarget();
        Optional<Rating> existing = ratingRepository.findByOrderIdAndTargetType(order.getOrderId(), target);

        Rating rating;
        if (existing.isPresent()) {
            rating = existing.get();
            if (rating.isUpdated()) {
                throw new FatumServiceException(CalificationErrorCode.RATING_ALREADY_UPDATED);
            }
            int previousScore = rating.getScore();
            rating.apply(request.getScore(), request.getDetails());
            moveAverage(rating, previousScore);
        } else {
            rating = new Rating(order.getOrderId(), target, request.getTargetId().trim(),
                    request.getRaterId().trim(), request.getScore(), request.getDetails());
            moveAverage(rating, null);
        }

        return RatingMapper.toResponse(ratingRepository.save(rating));
    }

    /**
     * Moves the average of the rated person by the difference, never by adding a new entry.
     *
     * <p>The initial 5 was already counted when the order was completed, so replacing it swaps the
     * score in the sum and keeps the count. A service has no instance of its own here, so its
     * rating is only stored.</p>
     */
    private void moveAverage(Rating rating, Integer previousScore) {
        if (rating.getTargetType() == RatingTarget.SERVICE) {
            return;
        }
        UserSnapshot user = snapshotService.userOrCreate(rating.getTargetId());
        if (previousScore == null) {
            user.register(rating.getScore());
        } else {
            user.replace(previousScore, rating.getScore());
        }
    }

    @Transactional(readOnly = true)
    public RatingResponse findById(String id) {
        return RatingMapper.toResponse(ratingRepository.findById(id)
                .orElseThrow(() -> new FatumServiceException(CalificationErrorCode.RATING_NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public List<RatingResponse> findByOrder(String orderId) {
        return RatingMapper.toResponses(ratingRepository.findByOrderIdOrderByCreatedAtAsc(orderId.trim()));
    }

    @Transactional(readOnly = true)
    public List<RatingResponse> findByTarget(RatingTarget target, String targetId) {
        return RatingMapper.toResponses(
                ratingRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(target, targetId.trim()));
    }
}
