package fatum.rate.model;

import fatum.rate.exception.CalificationErrorCode;
import fatum.rate.exception.FatumServiceException;
import fatum.rate.model.constant.RatingTarget;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RatingTest {

    @Test
    void theInitialRatingLooksRealButIsNotUpdated() {
        Rating rating = Rating.initial("o-1", RatingTarget.PROFESSIONAL, "p-1", "c-1");

        assertEquals(5, rating.getScore());
        assertFalse(rating.isUpdated());
        assertEquals("c-1", rating.getRaterId());
        assertEquals("p-1", rating.getTargetId());
        assertEquals(RatingTarget.PROFESSIONAL, rating.getTargetType());
        assertNull(rating.getDetails());
    }

    @Test
    void theUserRatingClosesTheInitialOne() {
        Rating rating = Rating.initial("o-1", RatingTarget.CLIENT, "c-1", "p-1");

        rating.apply(2, Map.of("disgusto", Map.of("calidad", "baja")));

        assertEquals(2, rating.getScore());
        assertTrue(rating.isUpdated());
        Map<?, ?> disgusto = (Map<?, ?>) rating.getDetails().get("disgusto");
        assertEquals("baja", disgusto.get("calidad"));
    }

    @Test
    void aRatingCannotBeUpdatedTwice() {
        Rating rating = Rating.initial("o-1", RatingTarget.SERVICE, "s-1", "c-1");
        rating.apply(3, null);

        FatumServiceException exception = assertThrows(FatumServiceException.class,
                () -> rating.apply(1, null));

        assertEquals(CalificationErrorCode.RATING_ALREADY_UPDATED, exception.getErrorCode());
        assertEquals(3, rating.getScore());
    }

    @Test
    void aRatingCreatedFromTheRequestIsBornUpdated() {
        Rating rating = new Rating("o-1", RatingTarget.SERVICE, "s-1", "c-1", 4, Map.of());

        assertTrue(rating.isUpdated());
        assertNull(rating.getDetails());
    }

    @Test
    void rejectsAScoreOutOfRange() {
        assertThrows(FatumServiceException.class,
                () -> new Rating("o-1", RatingTarget.CLIENT, "c-1", "p-1", 0, null));
        assertThrows(FatumServiceException.class,
                () -> new Rating("o-1", RatingTarget.CLIENT, "c-1", "p-1", 6, null));
        assertThrows(FatumServiceException.class,
                () -> Rating.initial("o-1", RatingTarget.CLIENT, "c-1", "p-1").apply(9, null));
    }

    @Test
    void rejectsABlankIdentifier() {
        assertThrows(FatumServiceException.class,
                () -> new Rating(" ", RatingTarget.CLIENT, "c-1", "p-1", 3, null));
        assertThrows(FatumServiceException.class,
                () -> new Rating("o-1", RatingTarget.CLIENT, " ", "p-1", 3, null));
        assertThrows(FatumServiceException.class,
                () -> Rating.initial("o-1", RatingTarget.CLIENT, "c-1", " "));
    }
}
