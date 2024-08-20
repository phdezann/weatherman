package org.phdezann.home.console.model;

import java.util.List;

import org.phdezann.home.console.core.ForecastData;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class ForecastSummary {
    private final List<ForecastData> values;
}
