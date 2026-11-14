package fatum.rate.event.listener;

import fatum.rate.event.EventDetail;
import fatum.rate.event.OrderEventType;
import fatum.rate.model.constant.OrderStatus;
import fatum.rate.service.OrderCompletionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);
    private final OrderCompletionService orderCompletionService;

    public OrderEventListener(OrderCompletionService orderCompletionService) {
        this.orderCompletionService = orderCompletionService;
    }

    public void handle(String detailType, EventDetail detail) {
        String orderId = detail.text("orderId");
        if (orderId == null) {
            log.warn("Order event without orderId ignored");
            return;
        }

        switch (detailType) {
            case OrderEventType.ORDER_COMPLETED -> complete(detail, orderId);
            case OrderEventType.ORDER_STATUS_CHANGED -> {
                // Kept as a fallback: the status event also tells when the order is completed.
                if (detail.enumValue("status", OrderStatus.class) == OrderStatus.COMPLETED) {
                    complete(detail, orderId);
                }
            }
            default -> log.debug("Order event ignored: {}", detailType);
        }
    }

    private void complete(EventDetail detail, String orderId) {
        orderCompletionService.complete(
                orderId,
                detail.text("serviceId"),
                detail.text("clientId"),
                detail.text("providerId"));
    }
}
