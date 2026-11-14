package fatum.rate.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.JsonNode;

/**
 * The event as EventBridge delivers it into SQS: the fact travels inside {@code detail}, under its
 * own {@code detail-type}, and the {@code source} says which service announced it.
 *
 * <p>Same shape CatalogService reads, so a rule that targets both services needs nothing new.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EventBridgeEnvelope(
        String source,
        @JsonProperty("detail-type") String detailType,
        JsonNode detail,
        String id,
        String time
) {
}
