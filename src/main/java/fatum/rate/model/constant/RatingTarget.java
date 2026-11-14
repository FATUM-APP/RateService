package fatum.rate.model.constant;

/**
 * What a rating is about. One completed order has exactly one of each.
 *
 * <ul>
 *   <li>{@link #PROFESSIONAL}: the professional that did the job.</li>
 *   <li>{@link #CLIENT}: the client that booked it.</li>
 *   <li>{@link #SERVICE}: the catalog item the order is about.</li>
 * </ul>
 */
public enum RatingTarget {
    CLIENT,
    PROFESSIONAL,
    SERVICE
}
