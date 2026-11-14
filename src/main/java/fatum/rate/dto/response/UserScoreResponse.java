package fatum.rate.dto.response;

import fatum.rate.model.constant.UserRole;

import java.math.BigDecimal;

/** The average of one person, with the count that backs it and whether it is still the seed. */
public record UserScoreResponse(
        String userId,
        String name,
        UserRole role,
        BigDecimal average,
        int count,
        boolean provisional
) {
}
