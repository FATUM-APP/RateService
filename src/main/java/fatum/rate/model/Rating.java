package fatum.rate.model;

import fatum.rate.configuration.FatumTime;
import fatum.rate.exception.CalificationErrorCode;
import fatum.rate.exception.FatumServiceException;
import fatum.rate.model.constant.RatingTarget;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * One rating of one target in one order.
 *
 * <p>A completed order is born with its three ratings already written: the real rater, the real
 * target, a score of 5 and {@code updated = false}. It looks exactly like a rating somebody sent,
 * except for that flag.</p>
 *
 * <p>The flag is the one-shot lock: the first time the user sends the rating it is applied and
 * {@code updated} becomes {@code true}; from then on the row is closed and a second attempt is
 * refused.</p>
 */
@Entity
@Table(name = "RATINGS", uniqueConstraints = @UniqueConstraint(
        name = "uk_ratings_order_target", columnNames = {"ORDER_ID", "TARGET_TYPE"}))
public class Rating {

    public static final int MIN_SCORE = 1;
    public static final int MAX_SCORE = 5;
    /** The score a completed order starts every one of its ratings with. */
    public static final int INITIAL_SCORE = 5;

    @Id
    @UuidGenerator
    @Column(name = "ID", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "ORDER_ID", length = 36, nullable = false, updatable = false)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "TARGET_TYPE", length = 20, nullable = false, updatable = false)
    private RatingTarget targetType;

    @Column(name = "TARGET_ID", length = 36, nullable = false, updatable = false)
    private String targetId;

    @Column(name = "RATER_ID", length = 36, nullable = false, updatable = false)
    private String raterId;

    @Column(name = "SCORE", nullable = false)
    private int score;

    /** False while the value is the one written at completion, true once the user sent theirs. */
    @Column(name = "UPDATED", nullable = false)
    private boolean updated;

    @Column(name = "DETAILS", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> details;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private OffsetDateTime createdAt = FatumTime.now();

    @Column(name = "UPDATED_AT", nullable = false)
    private OffsetDateTime updatedAt = FatumTime.now();

    protected Rating() {
        // JPA
    }

    /** A rating the user really sent: it is born already updated and can never be changed. */
    public Rating(String orderId, RatingTarget target, String targetId, String raterId,
                  int score, Map<String, Object> details) {
        this(orderId, target, targetId, raterId, score, details, true);
    }

    private Rating(String orderId, RatingTarget target, String targetId, String raterId,
                   int score, Map<String, Object> details, boolean updated) {
        requireText(orderId);
        requireText(targetId);
        requireText(raterId);
        if (target == null) {
            throw new FatumServiceException(CalificationErrorCode.BLANK);
        }
        validateScore(score);
        this.orderId = orderId.trim();
        this.targetType = target;
        this.targetId = targetId.trim();
        this.raterId = raterId.trim();
        this.score = score;
        this.updated = updated;
        this.details = emptyToNull(details);
    }

    /**
     * The rating a completed order writes so nothing is left unrated.
     *
     * <p>It carries the rater that would have written it — the client for the professional and the
     * service, the professional for the client — and the score 5. The user's later submission is an
     * update of this same row, not a new rating.</p>
     */
    public static Rating initial(String orderId, RatingTarget target, String targetId, String raterId) {
        return new Rating(orderId, target, targetId, raterId, INITIAL_SCORE, null, false);
    }

    /** Writes the real rating over the initial 5 and closes the row. */
    public void apply(int newScore, Map<String, Object> newDetails) {
        if (updated) {
            throw new FatumServiceException(CalificationErrorCode.RATING_ALREADY_UPDATED);
        }
        validateScore(newScore);
        this.score = newScore;
        this.details = emptyToNull(newDetails);
        this.updated = true;
        this.updatedAt = FatumTime.now();
    }

    private static void validateScore(int score) {
        if (score < MIN_SCORE || score > MAX_SCORE) {
            throw new FatumServiceException(CalificationErrorCode.INVALID_SCORE);
        }
    }

    private static void requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new FatumServiceException(CalificationErrorCode.BLANK);
        }
    }

    private static Map<String, Object> emptyToNull(Map<String, Object> value) {
        return value == null || value.isEmpty() ? null : value;
    }

    public String getId() { return id; }
    public String getOrderId() { return orderId; }
    public RatingTarget getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public String getRaterId() { return raterId; }
    public int getScore() { return score; }
    public boolean isUpdated() { return updated; }
    public Map<String, Object> getDetails() { return details; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
