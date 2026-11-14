package fatum.rate.service;

import fatum.rate.exception.CalificationErrorCode;
import fatum.rate.exception.FatumServiceException;
import fatum.rate.model.OrderSnapshot;
import fatum.rate.model.UserSnapshot;
import fatum.rate.model.constant.UserRole;
import fatum.rate.repository.OrderSnapshotRepository;
import fatum.rate.repository.UserSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SnapshotService {

    private final OrderSnapshotRepository orderRepository;
    private final UserSnapshotRepository userRepository;

    public SnapshotService(OrderSnapshotRepository orderRepository, UserSnapshotRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    // ========================================== ORDERS ==========================================

    /** The order is only stored when it is completed: before that there is nothing to rate. */
    @Transactional
    public OrderSnapshot createCompletedOrder(
            String orderId, String serviceId, String clientId, String providerId
    ) {
        OrderSnapshot order = orderRepository.findById(orderId)
                .orElseGet(() -> new OrderSnapshot(orderId, serviceId, clientId, providerId));
        order.describe(serviceId, clientId, providerId);
        order.complete();
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public OrderSnapshot requireOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new FatumServiceException(CalificationErrorCode.ORDER_NOT_FOUND));
    }

    // ========================================== USERS ==========================================

    /** Creates the instance of a user with its provisional 3, or updates what the event brought. */
    @Transactional
    public UserSnapshot upsertUser(String userId, String name, UserRole role) {
        UserSnapshot user = userRepository.findById(userId)
                .orElseGet(() -> new UserSnapshot(userId, name, role));
        user.refresh(name, role);
        return userRepository.save(user);
    }

    /**
     * The instance of the user that is being rated, created on the fly when no event brought it.
     *
     * <p>It is what keeps the rule "only register" true: a rating is never refused because
     * {@code USER_CREATED} did not arrive. The row is born with the provisional 3 and the first
     * rating replaces it.</p>
     */
    @Transactional
    public UserSnapshot userOrCreate(String userId) {
        return userRepository.findById(userId)
                .orElseGet(() -> userRepository.save(new UserSnapshot(userId, null, UserRole.CLIENT)));
    }

    @Transactional(readOnly = true)
    public UserSnapshot requireUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new FatumServiceException(CalificationErrorCode.USER_NOT_FOUND));
    }
}
