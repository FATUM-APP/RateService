package fatum.rate.model;

import fatum.rate.configuration.FatumTime;
import fatum.rate.exception.CalificationErrorCode;
import fatum.rate.exception.FatumServiceException;
import fatum.rate.model.constant.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

/**
 * The copy of a user this service keeps: identifier, name, role and the running average.
 *
 * <p>Only the sum and the count of the real ratings are stored, never the average and never the
 * seed. The 3 a new user starts with is what {@link #average()} answers while
 * {@link #getRatingsCount()} is zero, so the first real rating replaces it by itself and it can
 * never contaminate the sum.</p>
 */
@Entity
@Table(name = "USER_SNAPSHOTS")
public class UserSnapshot {

    public static final BigDecimal SEED_SCORE = new BigDecimal("3.00");

    @Id
    @Column(name = "USER_ID", length = 36, nullable = false, updatable = false)
    private String userId;

    @Column(name = "NAME", length = 142)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", length = 20, nullable = false)
    private UserRole role = UserRole.CLIENT;

    @Column(name = "TOTAL_SCORE", nullable = false)
    private long totalScore;

    @Column(name = "RATINGS_COUNT", nullable = false)
    private int ratingsCount;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    @Column(name = "CREATED_AT", nullable = false)
    private OffsetDateTime createdAt = FatumTime.now();

    @Column(name = "UPDATED_AT", nullable = false)
    private OffsetDateTime updatedAt = FatumTime.now();

    protected UserSnapshot() {
        // JPA
    }

    public UserSnapshot(String userId, String name, UserRole role) {
        if (userId == null || userId.isBlank()) {
            throw new FatumServiceException(CalificationErrorCode.BLANK);
        }
        this.userId = userId.trim();
        this.name = clean(name);
        this.role = role == null ? UserRole.CLIENT : role;
    }

    /** Writes only what really changed, so a repeated event does not bump the version. */
    public void refresh(String newName, UserRole newRole) {
        String cleanedName = clean(newName);
        if (cleanedName != null && !cleanedName.equals(name)) {
            this.name = cleanedName;
            this.updatedAt = FatumTime.now();
        }
        if (newRole != null && newRole != role) {
            this.role = newRole;
            this.updatedAt = FatumTime.now();
        }
    }

    /** Adds one rating: the first one replaces the seed, the rest are averaged. */
    public void register(int score) {
        validateScore(score);
        this.totalScore += score;
        this.ratingsCount++;
        this.updatedAt = FatumTime.now();
    }

    /**
     * Swaps one already counted score for another, without changing the count.
     *
     * <p>It is what happens when the placeholder 5 written at completion is replaced by the score
     * the user really sent: the rating was already in the average, so the average has to move by
     * the difference and not gain a new entry.</p>
     */
    public void replace(int previousScore, int newScore) {
        validateScore(previousScore);
        validateScore(newScore);
        if (ratingsCount == 0) {
            register(newScore);
            return;
        }
        this.totalScore = this.totalScore - previousScore + newScore;
        this.updatedAt = FatumTime.now();
    }

    /** The average of the real ratings, or the provisional 3 while there are none. */
    public BigDecimal average() {
        if (ratingsCount == 0) {
            return SEED_SCORE;
        }
        return BigDecimal.valueOf(totalScore)
                .divide(BigDecimal.valueOf(ratingsCount), 2, RoundingMode.HALF_UP);
    }

    public boolean isProvisional() {
        return ratingsCount == 0;
    }

    private static void validateScore(int score) {
        if (score < Rating.MIN_SCORE || score > Rating.MAX_SCORE) {
            throw new FatumServiceException(CalificationErrorCode.INVALID_SCORE);
        }
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public UserRole getRole() { return role; }
    public long getTotalScore() { return totalScore; }
    public int getRatingsCount() { return ratingsCount; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
