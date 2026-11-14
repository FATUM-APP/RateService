package fatum.rate.model;

import fatum.rate.exception.FatumServiceException;
import fatum.rate.model.constant.UserRole;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserSnapshotTest {

    @Test
    void startsWithTheProvisionalThree() {
        UserSnapshot user = new UserSnapshot("u-1", "Ana", UserRole.CLIENT);

        assertEquals(new BigDecimal("3.00"), user.average());
        assertTrue(user.isProvisional());
        assertEquals(0, user.getRatingsCount());
    }

    @Test
    void theFirstRatingReplacesTheSeed() {
        UserSnapshot user = new UserSnapshot("u-1", "Ana", UserRole.CLIENT);

        user.register(2);

        assertEquals(new BigDecimal("2.00"), user.average());
        assertFalse(user.isProvisional());
        assertEquals(1, user.getRatingsCount());
        assertEquals(2, user.getTotalScore());
    }

    @Test
    void laterRatingsAreAveraged() {
        UserSnapshot user = new UserSnapshot("u-1", "Ana", UserRole.CLIENT);

        user.register(2);
        user.register(4);

        assertEquals(new BigDecimal("3.00"), user.average());
        assertEquals(2, user.getRatingsCount());
    }

    @Test
    void replacingAScoreMovesTheAverageWithoutCountingAgain() {
        UserSnapshot user = new UserSnapshot("u-1", "Ana", UserRole.CLIENT);
        user.register(5);

        user.replace(5, 2);

        assertEquals(new BigDecimal("2.00"), user.average());
        assertEquals(1, user.getRatingsCount());
    }

    @Test
    void replacingOnAnEmptyAverageRegistersIt() {
        UserSnapshot user = new UserSnapshot("u-1", "Ana", UserRole.CLIENT);

        user.replace(5, 4);

        assertEquals(new BigDecimal("4.00"), user.average());
        assertEquals(1, user.getRatingsCount());
    }

    @Test
    void rejectsAScoreOutOfRange() {
        UserSnapshot user = new UserSnapshot("u-1", "Ana", UserRole.CLIENT);

        assertThrows(FatumServiceException.class, () -> user.register(0));
        assertThrows(FatumServiceException.class, () -> user.register(6));
    }

    @Test
    void rejectsABlankIdentifier() {
        assertThrows(FatumServiceException.class, () -> new UserSnapshot(" ", "Ana", UserRole.CLIENT));
    }

    @Test
    void refreshKeepsWhatTheEventDoesNotBring() {
        UserSnapshot user = new UserSnapshot("u-1", "Ana", UserRole.CLIENT);

        user.refresh(null, UserRole.PROFESSIONAL);

        assertEquals("Ana", user.getName());
        assertEquals(UserRole.PROFESSIONAL, user.getRole());
    }
}
