package fatum.rate.model;

import fatum.rate.exception.FatumServiceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderSnapshotTest {

    @Test
    void anOrderThatWasNotCompletedCannotBeRated() {
        OrderSnapshot order = new OrderSnapshot("o-1", "s-1", "c-1", "p-1");

        assertFalse(order.canBeRated());
    }

    @Test
    void completionOpensItForRatings() {
        OrderSnapshot order = new OrderSnapshot("o-1", "s-1", "c-1", "p-1");

        order.complete();

        assertTrue(order.canBeRated());
        assertTrue(order.isCompleted());
    }

    @Test
    void describeFillsWhatTheEventBroughtAndKeepsTheRest() {
        OrderSnapshot order = new OrderSnapshot("o-1", null, null, "p-1");

        order.describe("s-1", "c-1", null);

        assertEquals("s-1", order.getServiceId());
        assertEquals("c-1", order.getClientId());
        assertEquals("p-1", order.getProviderId());
    }

    @Test
    void describeDoesNotEraseAKnownValue() {
        OrderSnapshot order = new OrderSnapshot("o-1", "s-1", "c-1", "p-1");

        order.describe(null, " ", null);

        assertEquals("s-1", order.getServiceId());
        assertEquals("c-1", order.getClientId());
        assertEquals("p-1", order.getProviderId());
    }

    @Test
    void anEmptyPartyIsStoredAsNull() {
        OrderSnapshot order = new OrderSnapshot("o-1", "s-1", "  ", null);

        assertNull(order.getClientId());
        assertNull(order.getProviderId());
    }

    @Test
    void rejectsABlankIdentifier() {
        assertThrows(FatumServiceException.class, () -> new OrderSnapshot(" ", "s-1", "c-1", "p-1"));
    }
}
