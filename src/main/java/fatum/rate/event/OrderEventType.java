package fatum.rate.event;

public final class OrderEventType {

    public static final String SOURCE = "fatum.catalogservice";
    public static final String ORDER_CREATED = "ORDER_CREATED";
    public static final String ORDER_STATUS_CHANGED = "ORDER_STATUS_CHANGED";
    /** The event that opens the order for ratings and writes the three initial 5s. */
    public static final String ORDER_COMPLETED = "ORDER_COMPLETED";

    private OrderEventType() {
    }
}
