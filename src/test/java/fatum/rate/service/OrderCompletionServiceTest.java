package fatum.rate.service;

import fatum.rate.model.OrderSnapshot;
import fatum.rate.model.Rating;
import fatum.rate.model.UserSnapshot;
import fatum.rate.model.constant.RatingTarget;
import fatum.rate.model.constant.UserRole;
import fatum.rate.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderCompletionServiceTest {

    private SnapshotService snapshotService;
    private RatingRepository ratingRepository;
    private OrderCompletionService orderCompletionService;

    @BeforeEach
    void setUp() {
        snapshotService = mock(SnapshotService.class);
        ratingRepository = mock(RatingRepository.class);
        orderCompletionService = new OrderCompletionService(snapshotService, ratingRepository);
        when(snapshotService.createCompletedOrder(anyString(), any(), any(), any()))
                .thenAnswer(invocation -> {
                    OrderSnapshot order = new OrderSnapshot(
                            invocation.getArgument(0), invocation.getArgument(1),
                            invocation.getArgument(2), invocation.getArgument(3));
                    order.complete();
                    return order;
                });
        when(snapshotService.userOrCreate(anyString()))
                .thenAnswer(invocation -> new UserSnapshot(invocation.getArgument(0), null, UserRole.CLIENT));
        when(ratingRepository.findByOrderIdAndTargetType(anyString(), any())).thenReturn(Optional.empty());
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void writesTheThreeRatingsWithTheirRealRatersAtFive() {
        orderCompletionService.complete("o-1", "s-1", "c-1", "p-1");

        ArgumentCaptor<Rating> captor = ArgumentCaptor.forClass(Rating.class);
        verify(ratingRepository, times(3)).save(captor.capture());
        List<Rating> ratings = captor.getAllValues();

        assertEquals(3, ratings.size());
        ratings.forEach(rating -> {
            assertEquals(Rating.INITIAL_SCORE, rating.getScore());
            assertFalse(rating.isUpdated());
            assertEquals("o-1", rating.getOrderId());
        });

        // el cliente califica al profesional
        assertEquals(RatingTarget.PROFESSIONAL, ratings.get(0).getTargetType());
        assertEquals("p-1", ratings.get(0).getTargetId());
        assertEquals("c-1", ratings.get(0).getRaterId());

        // el profesional califica al cliente
        assertEquals(RatingTarget.CLIENT, ratings.get(1).getTargetType());
        assertEquals("c-1", ratings.get(1).getTargetId());
        assertEquals("p-1", ratings.get(1).getRaterId());

        // el cliente califica el servicio
        assertEquals(RatingTarget.SERVICE, ratings.get(2).getTargetType());
        assertEquals("s-1", ratings.get(2).getTargetId());
        assertEquals("c-1", ratings.get(2).getRaterId());
    }

    @Test
    void theProfessionalAndTheClientStartWithFiveInTheirAverage() {
        orderCompletionService.complete("o-1", "s-1", "c-1", "p-1");

        verify(snapshotService).userOrCreate("p-1");
        verify(snapshotService).userOrCreate("c-1");
        verify(snapshotService, never()).userOrCreate("s-1");
    }

    @Test
    void aRepeatedEventDoesNotOverwriteWhatIsAlreadyThere() {
        when(ratingRepository.findByOrderIdAndTargetType(anyString(), any()))
                .thenReturn(Optional.of(Rating.initial("o-1", RatingTarget.PROFESSIONAL, "p-1", "c-1")));

        orderCompletionService.complete("o-1", "s-1", "c-1", "p-1");

        verify(ratingRepository, never()).save(any());
        verify(snapshotService, never()).userOrCreate(anyString());
    }

    @Test
    void withoutAClientNoneOfTheThreeCanBeWritten() {
        orderCompletionService.complete("o-1", "s-1", null, "p-1");

        verify(ratingRepository, never()).save(any());
    }

    @Test
    void withoutAProfessionalOnlyTheServiceRatingIsWritten() {
        orderCompletionService.complete("o-1", "s-1", "c-1", null);

        ArgumentCaptor<Rating> captor = ArgumentCaptor.forClass(Rating.class);
        verify(ratingRepository).save(captor.capture());
        assertEquals(RatingTarget.SERVICE, captor.getValue().getTargetType());
        assertEquals("c-1", captor.getValue().getRaterId());
    }
}
