package fatum.rate.configuration;

import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * The single clock of the service.
 *
 * <p>Every timestamp is written in the time zone of the platform, with its offset, so a row read in
 * another zone does not need to guess which one it was written in. Centralising it here means the
 * day the zone changes, it changes once.</p>
 */
public final class FatumTime {

    public static final ZoneId ZONE = ZoneId.of("America/Bogota");

    private FatumTime() {
    }

    public static OffsetDateTime now() {
        return OffsetDateTime.now(ZONE);
    }
}
