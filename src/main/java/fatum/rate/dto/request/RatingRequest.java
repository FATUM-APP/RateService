package fatum.rate.dto.request;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import fatum.rate.model.Rating;
import fatum.rate.model.constant.RatingTarget;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

/** Only identity of the rating and score are interpreted; extra GUI fields are preserved. */
public class RatingRequest {

    @NotBlank
    private String orderId;

    @NotBlank
    private String raterId;

    @NotNull
    private RatingTarget target;

    @NotBlank
    private String targetId;

    @NotNull
    @Min(Rating.MIN_SCORE)
    @Max(Rating.MAX_SCORE)
    private Integer score;

    private Map<String, Object> details = new LinkedHashMap<>();

    @JsonAnySetter
    public void putDetail(String key, Object value) {
        details.put(key, value);
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getRaterId() { return raterId; }
    public void setRaterId(String raterId) { this.raterId = raterId; }
    public RatingTarget getTarget() { return target; }
    public void setTarget(RatingTarget target) { this.target = target; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) {
        this.details = details == null ? new LinkedHashMap<>() : new LinkedHashMap<>(details);
    }
}
