package fatum.rate.dto.mapper;

import fatum.rate.dto.response.RatingResponse;
import fatum.rate.model.Rating;

import java.util.List;

public final class RatingMapper {

    private RatingMapper() {
    }

    public static RatingResponse toResponse(Rating rating) {
        return new RatingResponse(
                rating.getId(), rating.getOrderId(), rating.getTargetType(), rating.getTargetId(),
                rating.getRaterId(), rating.getScore(), rating.isUpdated(), rating.getDetails(),
                rating.getCreatedAt());
    }

    public static List<RatingResponse> toResponses(List<Rating> ratings) {
        return ratings.stream().map(RatingMapper::toResponse).toList();
    }
}
