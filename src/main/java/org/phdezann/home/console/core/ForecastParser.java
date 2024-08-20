package org.phdezann.home.console.core;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.phdezann.home.console.json.ForecastRoot;
import org.phdezann.home.console.model.ForecastSummary;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ForecastParser {

    private final AppArgs appArgs;
    private final JsonSerializer jsonSerializer;
    private final NextHourCalculator nextHourCalculator;

    public ForecastSummary build(String json, ZonedDateTime now) {
        if (appArgs.isFakeWeatherData()) {
            var from = nextHourCalculator.getNext(now, now.getHour() + 1);
            var to = from.plusHours(1);
            return new ForecastSummary(List.of(new ForecastData(from, to, 1.0, 1.0, 1000)));
        }

        var thisHour = now //
                .withMinute(0) //
                .withSecond(0) //
                .withNano(0);

        var nextHour = thisHour //
                .plusHours(1);

        var next0600 = nextHourCalculator.getNext(now, 6);
        var next1200 = nextHourCalculator.getNext(now, 12);
        var next1800 = nextHourCalculator.getNext(now, 18);
        var root = jsonSerializer.readValue(json, ForecastRoot.class);

        var values = new ArrayList<ForecastData>();

        findHour(root, thisHour).ifPresent(values::add);
        findHour(root, nextHour).ifPresent(values::add);
        findHour(root, next0600).ifPresent(values::add);
        findHour(root, next1200).ifPresent(values::add);
        findHour(root, next1800).ifPresent(values::add);

        var newValues = values //
                .stream() //
                .distinct() //
                .sorted(Comparator.comparing(ForecastData::getFrom)) //
                .toList();

        return new ForecastSummary(newValues);
    }

    public ForecastSummary buildNext6Hours(String json, ZonedDateTime now) {
        if (appArgs.isFakeWeatherData()) {
            var from = nextHourCalculator.getNext(now, now.getHour() + 1);
            var to = from.plusHours(1);
            return new ForecastSummary(List.of(new ForecastData(from, to, 1.0, 1.0, 1000)));
        }

        var thisHour = now //
                .withMinute(0) //
                .withSecond(0) //
                .withNano(0);

        var plus1Hour = thisHour.plusHours(1);
        var plus2Hours = thisHour.plusHours(2);
        var plus3Hours = thisHour.plusHours(3);
        var plus4Hours = thisHour.plusHours(4);
        var plus5Hours = thisHour.plusHours(5);

        var root = jsonSerializer.readValue(json, ForecastRoot.class);

        var values = new ArrayList<ForecastData>();

        findHour(root, thisHour).ifPresent(values::add);
        findHour(root, plus1Hour).ifPresent(values::add);
        findHour(root, plus2Hours).ifPresent(values::add);
        findHour(root, plus3Hours).ifPresent(values::add);
        findHour(root, plus4Hours).ifPresent(values::add);
        findHour(root, plus5Hours).ifPresent(values::add);

        var newValues = values //
                .stream() //
                .distinct() //
                .sorted(Comparator.comparing(ForecastData::getFrom)) //
                .toList();

        return new ForecastSummary(newValues);
    }

    private static Optional<ForecastData> findHour(ForecastRoot root, ZonedDateTime nextHour) {
        var entries = root.getTimelines().getEntries();
        for (int i = 0; i < entries.size() - 1; i++) {
            var entry = entries.get(i);
            if (entry.getTime().toInstant().equals(nextHour.toInstant())) {
                var previousEntry = entries.get(i + 1);
                var precipitation = Double.parseDouble(entry.getValues().get("precipitationProbability"));
                var temperature = Double.parseDouble(entry.getValues().get("temperatureApparent"));
                var weather = Integer.parseInt(entry.getValues().get("weatherCode"));
                var from = entry.getTime().withZoneSameInstant(ZoneId.of("Europe/Paris"));
                var to = previousEntry.getTime().withZoneSameInstant(ZoneId.of("Europe/Paris"));
                return Optional.of(new ForecastData(from, to, precipitation, temperature, weather));
            }
        }
        return Optional.empty();
    }

}
