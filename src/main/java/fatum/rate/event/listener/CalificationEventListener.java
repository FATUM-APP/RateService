package fatum.rate.event.listener;

import fatum.rate.event.EventDispatcher;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * The only entry point of the queue.
 *
 * <p>One listener and not one per source, because two pollers on the same queue do not split the
 * work by topic — each message goes to whichever poller gets it first. The {@code source} of the
 * envelope is what decides, and it is also what an EventBridge rule filters on; that decision lives
 * in {@link EventDispatcher}.</p>
 *
 * <p>In production the listener is on, and the application refuses to start if the queue cannot be
 * reached: a consumer that silently runs without consuming is worse than one that does not start.
 * For local development, {@code SQS_ENABLED=false} keeps the API up without AWS.</p>
 */
@Component
@ConditionalOnProperty(name = "fatum.sqs.enabled", havingValue = "true", matchIfMissing = true)
public class CalificationEventListener {

    private final EventDispatcher eventDispatcher;

    public CalificationEventListener(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @SqsListener("${fatum.sqs.calification-events-queue-name}")
    public void handle(String message) {
        eventDispatcher.dispatch(message);
    }
}
