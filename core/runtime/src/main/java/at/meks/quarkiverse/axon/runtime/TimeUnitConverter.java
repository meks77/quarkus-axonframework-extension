package at.meks.quarkiverse.axon.runtime;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;

public class TimeUnitConverter {

    private TimeUnitConverter() {
    }

    public static TemporalUnit toTemporalUnit(TimeUnit timeUnit) {
        return switch (timeUnit) {
            case NANOSECONDS -> ChronoUnit.NANOS;
            case MICROSECONDS -> ChronoUnit.MICROS;
            case MILLISECONDS -> ChronoUnit.MILLIS;
            case SECONDS -> ChronoUnit.SECONDS;
            case MINUTES -> ChronoUnit.MINUTES;
            case HOURS -> ChronoUnit.HOURS;
            case DAYS -> ChronoUnit.DAYS;
        };
    }
}