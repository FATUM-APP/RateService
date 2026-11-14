package fatum.rate.service;

import fatum.rate.model.OrderSnapshot;
import fatum.rate.model.Rating;
import fatum.rate.model.constant.RatingTarget;
import fatum.rate.repository.RatingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * What happens when an order is completed.
 *
 * <p>The order is stored here for the first time and its three ratings are written immediately:
 * the professional rated by the client, the client rated by the professional and the service rated
 * by the client. They carry the real rater and the real target, score 5 and {@code updated = false},
 * so they look like a rating somebody sent until the user replaces them.</p>
 */
@Service
public class OrderCompletionService {

    private static final Logger log = LoggerFactory.getLogger(OrderCompletionService.class);

    private final SnapshotService snapshotService;
    private final RatingRepository ratingRepository;

    public OrderCompletionService(SnapshotService snapshotService, RatingRepository ratingRepository) {
        this.snapshotService = snapshotService;
        this.ratingRepository = ratingRepository;
    }

    @Transactional
    public OrderSnapshot complete(String orderId, String serviceId, String clientId, String providerId) {
        OrderSnapshot order = snapshotService.createCompletedOrder(orderId, serviceId, clientId, providerId);

        seed(order, RatingTarget.PROFESSIONAL, order.getProviderId(), order.getClientId());
        seed(order, RatingTarget.CLIENT, order.getClientId(), order.getProviderId());
        seed(order, RatingTarget.SERVICE, order.getServiceId(), order.getClientId());

        log.info("Order {} completed: professional, client and service initialised at {}",
                order.getOrderId(), Rating.INITIAL_SCORE);
        return order;
    }

    /**
     * Writes the initial rating unless the order already has one for that target.
     *
     * <p>It is idempotent on purpose: a redelivery of {@code ORDER_COMPLETED} finds the row and
     * leaves it alone, so an initial 5 never overwrites a score the user already sent.</p>
     */
    private void seed(OrderSnapshot order, RatingTarget target, String targetId, String raterId) {
        if (targetId == null || raterId == null) {
            log.warn("Order {} does not know the {} id or its rater: that rating is not initialised",
                    order.getOrderId(), target);
            return;
        }
        if (ratingRepository.findByOrderIdAndTargetType(order.getOrderId(), target).isPresent()) {
            return;
        }
        ratingRepository.save(Rating.initial(order.getOrderId(), target, targetId, raterId));
        if (target != RatingTarget.SERVICE) {
            snapshotService.userOrCreate(targetId).register(Rating.INITIAL_SCORE);
        }
    }
}
