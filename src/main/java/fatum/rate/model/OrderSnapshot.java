package fatum.rate.model;

import fatum.rate.exception.CalificationErrorCode;
import fatum.rate.exception.FatumServiceException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * The local copy of an order: who it belongs to, which service it is about and whether it was
 * completed.
 *
 * <p>It only exists from the moment the order is completed. Before that there is nothing to rate,
 * and a request to rate it answers {@code ORDER_NOT_FOUND}.</p>
 */
@Entity
@Table(name = "ORDER_SNAPSHOTS")
public class OrderSnapshot {

    @Id
    @Column(name = "ORDER_ID", length = 36, nullable = false, updatable = false)
    private String orderId;

    @Column(name = "SERVICE_ID", length = 36)
    private String serviceId;

    @Column(name = "CLIENT_ID", length = 36)
    private String clientId;

    @Column(name = "PROVIDER_ID", length = 36)
    private String providerId;

    @Column(name = "IS_COMPLETED", nullable = false)
    private boolean completed;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    protected OrderSnapshot() {
        // JPA
    }

    public OrderSnapshot(String orderId, String serviceId, String clientId, String providerId) {
        if (orderId == null || orderId.isBlank()) {
            throw new FatumServiceException(CalificationErrorCode.BLANK);
        }
        this.orderId = orderId.trim();
        this.serviceId = clean(serviceId);
        this.clientId = clean(clientId);
        this.providerId = clean(providerId);
    }

    /** Fills what a later event brought; an absent value never erases one already known. */
    public void describe(String newServiceId, String newClientId, String newProviderId) {
        this.serviceId = merge(serviceId, newServiceId);
        this.clientId = merge(clientId, newClientId);
        this.providerId = merge(providerId, newProviderId);
    }

    public void complete() {
        this.completed = true;
    }

    public boolean canBeRated() {
        return completed;
    }

    private static String merge(String current, String candidate) {
        String cleaned = clean(candidate);
        return cleaned == null ? current : cleaned;
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public String getOrderId() {
        return orderId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getProviderId() {
        return providerId;
    }

    public boolean isCompleted() {
        return completed;
    }
}
