package org.phdezann.home.console.model;

import java.time.ZonedDateTime;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class RealtimeSummary {
    private final ZonedDateTime zonedDateTime;
    private final double precipitationProbability;
    private final double temperatureApparent;
    private final int weatherCode;
}
