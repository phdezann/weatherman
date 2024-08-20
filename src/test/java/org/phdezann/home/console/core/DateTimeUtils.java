package org.phdezann.home.console.core;

import java.time.ZonedDateTime;

public class DateTimeUtils {

    public static ZonedDateTime toEuropeParisTZ(String datetime) {
        return ZonedDateTime.parse(datetime + "+01:00[Europe/Paris]");
    }

}
