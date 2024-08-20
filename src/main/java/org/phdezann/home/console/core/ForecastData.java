package org.phdezann.home.console.core;

import java.time.ZonedDateTime;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class ForecastData {
    private final ZonedDateTime from;
    private final ZonedDateTime to;
    private final double precipitationProbability;
    private final double temperatureApparent;
    private final int weatherCode;
}
