package fatum.rate.dto.response;

import fatum.rate.model.constant.RatingTarget;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * One rating as it is read back.
 *
 * <p>{@code updated} is false while the value is the 5 written when the order was completed, and
 * true once the user sent theirs. From that moment the row is closed.</p>
 */
public record RatingResponse(
        String id,
        String orderId,
        RatingTarget target,
        String targetId,
        String raterId,
        int score,
        boolean updated,
        Map<String, Object> details,
        OffsetDateTime createdAt
) {
}
