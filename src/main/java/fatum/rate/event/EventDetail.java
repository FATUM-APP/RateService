package fatum.rate.event;

import tools.jackson.databind.JsonNode;

import java.util.Locale;

/**
 * Reads the fields of a {@code detail} without throwing on what is missing.
 *
 * <p>A consumer that throws on a field it does not find turns one malformed message into an endless
 * redelivery. Here a missing field is {@code null} and the listener decides: some are fatal for that
 * message (and are logged as such), others are simply not there yet.</p>
 */
public final class EventDetail {

    private final JsonNode node;

    private EventDetail(JsonNode node) {
        this.node = node;
    }

    public static EventDetail of(JsonNode node) {
        return new EventDetail(node);
    }

    /** The field as trimmed text, or {@code null} when it is missing, null or blank. */
    public String text(String field) {
        if (node == null || !node.hasNonNull(field)) {
            return null;
        }
        String value = node.get(field).asText();
        return value == null || value.isBlank() ? null : value.trim();
    }

    /**
     * The field as an enum, or {@code null} when it is missing or is not one of the values.
     *
     * <p>An unknown status is not read as "completed": it is {@code null} and the listener skips it,
     * because guessing the state of an order is how an order that was never delivered ends up being
     * rated.</p>
     */
    public <E extends Enum<E>> E enumValue(String field, Class<E> type) {
        String raw = text(field);
        if (raw == null) {
            return null;
        }
        try {
            return Enum.valueOf(type, raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException notAnEnum) {
            return null;
        }
    }

    public boolean has(String field) {
        return text(field) != null;
    }

    /**
     * The first field that is present, in the order given.
     *
     * <p>It exists for names that two services may spell differently ({@code userName} against
     * {@code name}): the listener reads either one instead of failing on a detail nobody promised.</p>
     */
    public String firstText(String... fields) {
        for (String field : fields) {
            String value = text(field);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.valueOf(node);
    }
}
