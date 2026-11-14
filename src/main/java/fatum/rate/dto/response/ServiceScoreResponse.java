package fatum.rate.dto.response;

import java.math.BigDecimal;

/** The average of one catalog item, with the number of completed orders behind it. */
public record ServiceScoreResponse(
        String serviceId,
        BigDecimal average,
        int count,
        boolean provisional
) {
}
