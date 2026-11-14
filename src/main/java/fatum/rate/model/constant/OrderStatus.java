package fatum.rate.model.constant;

/**
 * The states of an order, copied from CatalogService because they travel in the events.
 *
 * <p>This service never moves an order: it only remembers the last state it was told about. The
 * mirror is needed so an unknown status cannot be silently read as "completed".</p>
 */
public enum OrderStatus {

    PENDING,
    ACCEPTED,
    REJECTED,
    DELIVERING,
    NOT_DELIVERED,
    NOT_PAYED,
    COMPLETED,
    CANCELLED,
    DISPUTED,
    SOLVED,
    REFUNDED
}
