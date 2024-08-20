package org.phdezann.home.console.core;

import java.time.ZonedDateTime;

public class NextHourCalculator {

    public ZonedDateTime getNext(ZonedDateTime dateTime, int hour) {
        var nextHour = dateTime //
                .withHour(hour) //
                .withMinute(0) //
                .withSecond(0) //
                .withNano(0);

        if (nextHour.isBefore(dateTime) || nextHour.isEqual(dateTime)) {
            return nextHour.plusDays(1);
        } else {
            return nextHour;
        }
    }

}
