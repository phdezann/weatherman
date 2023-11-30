package org.phdezann.weather.model;

import java.util.List;

import org.phdezann.weather.core.ForecastData;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class ForecastSummary {
    private final List<ForecastData> values;
}
