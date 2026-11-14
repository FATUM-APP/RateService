package fatum.rate.event.listener;

import fatum.rate.event.EventDetail;
import fatum.rate.event.UserEventType;
import fatum.rate.model.constant.UserRole;
import fatum.rate.service.SnapshotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserEventListener.class);
    private final SnapshotService snapshotService;

    public UserEventListener(SnapshotService snapshotService) {
        this.snapshotService = snapshotService;
    }

    public void handle(String detailType, EventDetail detail) {
        String userId = detail.text("userId");
        if (userId == null) {
            log.warn("User event without userId ignored");
            return;
        }

        switch (detailType) {
            case UserEventType.USER_CREATED -> {
                UserRole role = detail.enumValue("userRole", UserRole.class);
                snapshotService.upsertUser(userId, detail.firstText("userName", "name"),
                        role == null ? UserRole.CLIENT : role);
                log.info("User instance ready with its provisional 3: {}", userId);
            }
            case UserEventType.USER_BECAME_PROFESSIONAL -> changeRole(detail, userId, UserRole.PROFESSIONAL);
            case UserEventType.PROFESSIONAL_BECAME_CLIENT -> changeRole(detail, userId, UserRole.CLIENT);
            default -> log.debug("User event ignored: {}", detailType);
        }
    }

    private void changeRole(EventDetail detail, String userId, UserRole role) {
        snapshotService.upsertUser(userId, detail.firstText("userName", "name"), role);
        log.info("User role updated: {} as {}", userId, role);
    }
}
