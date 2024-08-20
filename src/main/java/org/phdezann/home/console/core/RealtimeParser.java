package org.phdezann.home.console.core;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.phdezann.home.console.json.TimelineRoot;
import org.phdezann.home.console.model.RealtimeSummary;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RealtimeParser {

    private final AppArgs appArgs;
    private final JsonSerializer jsonSerializer;

    public RealtimeSummary parse(String json) {
        if (appArgs.isFakeWeatherData()) {
            return new RealtimeSummary(ZonedDateTime.now(), 0.0, 0.0, 1000);
        }

        var root = jsonSerializer.readValue(json, TimelineRoot.class);
        var time = root.getEntry().getTime().withZoneSameInstant(ZoneId.of("Europe/Paris"));

        var values = root.getEntry().getValues();
        return new RealtimeSummary(time, //
                Double.parseDouble(values.get("precipitationProbability")), //
                Double.parseDouble(values.get("temperatureApparent")), //
                Integer.parseInt(values.get("weatherCode")));
    }
}
