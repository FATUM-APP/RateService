package fatum.rate.event;

import fatum.rate.event.listener.OrderEventListener;
import fatum.rate.event.listener.UserEventListener;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EventDispatcher {

    private static final Logger log = LoggerFactory.getLogger(EventDispatcher.class);

    private final ObjectMapper objectMapper;
    private final OrderEventListener orderEventListener;
    private final UserEventListener userEventListener;

    public EventDispatcher(
            ObjectMapper objectMapper,
            OrderEventListener orderEventListener,
            UserEventListener userEventListener
    ) {
        this.objectMapper = objectMapper;
        this.orderEventListener = orderEventListener;
        this.userEventListener = userEventListener;
    }

    public void dispatch(String message) {
        try {
            dispatch(objectMapper.readValue(message, EventBridgeEnvelope.class));
        } catch (JacksonException exception) {
            log.warn("Invalid EventBridge message ignored", exception);
        }
    }

    public void dispatch(EventBridgeEnvelope envelope) {
        if (envelope == null || envelope.source() == null || envelope.detailType() == null) {
            log.warn("Event without source or detail-type ignored");
            return;
        }
        EventDetail detail = EventDetail.of(envelope.detail());
        switch (envelope.source()) {
            case UserEventType.SOURCE -> userEventListener.handle(envelope.detailType(), detail);
            case OrderEventType.SOURCE -> orderEventListener.handle(envelope.detailType(), detail);
            default -> log.debug("Event source ignored: {}", envelope.source());
        }
    }
}
