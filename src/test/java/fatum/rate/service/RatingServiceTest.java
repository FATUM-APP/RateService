package fatum.rate.service;

import fatum.rate.dto.request.RatingRequest;
import fatum.rate.dto.response.RatingResponse;
import fatum.rate.exception.CalificationErrorCode;
import fatum.rate.exception.FatumServiceException;
import fatum.rate.model.OrderSnapshot;
import fatum.rate.model.Rating;
import fatum.rate.model.UserSnapshot;
import fatum.rate.model.constant.RatingTarget;
import fatum.rate.model.constant.UserRole;
import fatum.rate.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RatingServiceTest {

    private RatingRepository ratingRepository;
    private SnapshotService snapshotService;
    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        ratingRepository = mock(RatingRepository.class);
        snapshotService = mock(SnapshotService.class);
        ratingService = new RatingService(ratingRepository, snapshotService);
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private RatingRequest request(RatingTarget target, String targetId, int score) {
        RatingRequest request = new RatingRequest();
        request.setOrderId("o-1");
        request.setRaterId("c-1");
        request.setTarget(target);
        request.setTargetId(targetId);
        request.setScore(score);
        return request;
    }

    private OrderSnapshot completedOrder() {
        OrderSnapshot order = new OrderSnapshot("o-1", "s-1", "c-1", "p-1");
        order.complete();
        return order;
    }

    @Test
    void refusesAnOrderThatWasNotCompleted() {
        when(snapshotService.requireOrder("o-1"))
                .thenReturn(new OrderSnapshot("o-1", "s-1", "c-1", "p-1"));

        FatumServiceException exception = assertThrows(FatumServiceException.class,
                () -> ratingService.register(request(RatingTarget.PROFESSIONAL, "p-1", 2)));

        assertEquals(CalificationErrorCode.ORDER_NOT_COMPLETED, exception.getErrorCode());
        verify(ratingRepository, never()).save(any());
    }

    @Test
    void refusesAnOrderItDoesNotKnow() {
        when(snapshotService.requireOrder("o-1"))
                .thenThrow(new FatumServiceException(CalificationErrorCode.ORDER_NOT_FOUND));

        FatumServiceException exception = assertThrows(FatumServiceException.class,
                () -> ratingService.register(request(RatingTarget.PROFESSIONAL, "p-1", 2)));

        assertEquals(CalificationErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
        verifyNoInteractions(ratingRepository);
    }

    @Test
    void replacesTheInitialFiveAndMovesTheAverage() {
        when(snapshotService.requireOrder("o-1")).thenReturn(completedOrder());
        when(ratingRepository.findByOrderIdAndTargetType("o-1", RatingTarget.PROFESSIONAL))
                .thenReturn(Optional.of(Rating.initial("o-1", RatingTarget.PROFESSIONAL, "p-1", "c-1")));
        UserSnapshot professional = new UserSnapshot("p-1", null, UserRole.PROFESSIONAL);
        professional.register(Rating.INITIAL_SCORE);
        when(snapshotService.userOrCreate("p-1")).thenReturn(professional);

        RatingResponse response = ratingService.register(request(RatingTarget.PROFESSIONAL, "p-1", 2));

        assertEquals(2, response.score());
        assertTrue(response.updated());
        assertEquals(new BigDecimal("2.00"), professional.average());
        assertEquals(1, professional.getRatingsCount());
    }

    @Test
    void refusesASecondUpdateOfTheSameRating() {
        when(snapshotService.requireOrder("o-1")).thenReturn(completedOrder());
        Rating alreadyUpdated = Rating.initial("o-1", RatingTarget.PROFESSIONAL, "p-1", "c-1");
        alreadyUpdated.apply(4, null);
        when(ratingRepository.findByOrderIdAndTargetType("o-1", RatingTarget.PROFESSIONAL))
                .thenReturn(Optional.of(alreadyUpdated));

        FatumServiceException exception = assertThrows(FatumServiceException.class,
                () -> ratingService.register(request(RatingTarget.PROFESSIONAL, "p-1", 1)));

        assertEquals(CalificationErrorCode.RATING_ALREADY_UPDATED, exception.getErrorCode());
        assertEquals(4, alreadyUpdated.getScore());
        verify(ratingRepository, never()).save(any());
    }

    @Test
    void createsTheRowWhenTheOrderHasNoneForThatTarget() {
        when(snapshotService.requireOrder("o-1")).thenReturn(completedOrder());
        when(ratingRepository.findByOrderIdAndTargetType("o-1", RatingTarget.CLIENT))
                .thenReturn(Optional.empty());
        UserSnapshot client = new UserSnapshot("c-1", null, UserRole.CLIENT);
        when(snapshotService.userOrCreate("c-1")).thenReturn(client);

        RatingResponse response = ratingService.register(request(RatingTarget.CLIENT, "c-1", 4));

        assertEquals(4, response.score());
        assertTrue(response.updated());
        assertEquals(new BigDecimal("4.00"), client.average());
    }

    @Test
    void aServiceRatingDoesNotTouchAnyUserAverage() {
        when(snapshotService.requireOrder("o-1")).thenReturn(completedOrder());
        when(ratingRepository.findByOrderIdAndTargetType("o-1", RatingTarget.SERVICE))
                .thenReturn(Optional.of(Rating.initial("o-1", RatingTarget.SERVICE, "s-1", "c-1")));

        RatingResponse response = ratingService.register(request(RatingTarget.SERVICE, "s-1", 3));

        assertEquals(3, response.score());
        assertTrue(response.updated());
        verify(snapshotService, never()).userOrCreate(any());
    }

    @Test
    void storesTheDetailsTheGuiSent() {
        when(snapshotService.requireOrder("o-1")).thenReturn(completedOrder());
        when(ratingRepository.findByOrderIdAndTargetType("o-1", RatingTarget.SERVICE))
                .thenReturn(Optional.empty());

        RatingRequest request = request(RatingTarget.SERVICE, "s-1", 2);
        request.putDetail("disgusto", "tarde");
        ratingService.register(request);

        ArgumentCaptor<Rating> captor = ArgumentCaptor.forClass(Rating.class);
        verify(ratingRepository).save(captor.capture());
        assertTrue(captor.getValue().getDetails().containsKey("disgusto"));
    }
}
