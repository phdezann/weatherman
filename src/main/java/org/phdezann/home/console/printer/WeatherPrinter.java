package org.phdezann.home.console.printer;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.phdezann.home.console.core.AsciiProgressBarBuilder;
import org.phdezann.home.console.core.ForecastData;
import org.phdezann.home.console.model.ForecastSummary;
import org.phdezann.home.console.model.RealtimeSummary;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WeatherPrinter extends AbstractPrinter {

    private final Properties properties;
    private final AsciiProgressBarBuilder asciiProgressBarBuilder;

    public String print(Optional<String> ppap) {
        var lines = new ArrayList<String>();
        lines.add(toLineLayoutV1("", "", toPpapStr(ppap)));
        ppap.ifPresent(value -> {
            var intPpap = Integer.parseInt(value);
            lines.addAll(asciiProgressBarBuilder.build(6000, intPpap,  19, 6));
        });
        return String.join("\n", lines);
    }

    public String print(RealtimeSummary realtime, ForecastSummary forecast, Optional<Double> temperature,
            Optional<String> ppap, ZonedDateTime now) {
        var lines = new ArrayList<String>();
        lines.add(toHeader(temperature, ppap, now));
        lines.add(toLine(realtime));
        lines.addAll(toLines(forecast, now));
        lines.forEach(this::checkLength);
        return String.join("\n", lines);
    }

    public String printNext6Hours(ForecastSummary forecast, Optional<Double> temperature,
            Optional<String> ppap, ZonedDateTime now) {
        var lines = new ArrayList<String>();
        lines.add(toHeader(temperature, ppap, now));
        lines.addAll(toLines(forecast, now));
        lines.forEach(this::checkLength);
        return String.join("\n", lines);
    }

    private String toHeader(Optional<Double> temperature, Optional<String> ppap, ZonedDateTime now) {
        return toLineLayoutV1(toDateTimeStr(now), toTemperatureStr(temperature), toPpapStr(ppap));
    }

    private String toDateTimeStr(ZonedDateTime now) {
        var formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        return now.toLocalDateTime().format(formatter);
    }

    private String toTemperatureStr(Optional<Double> temperature) {
        return temperature.map(value -> Math.round(value) + "°").orElse("");
    }

    private String toPpapStr(Optional<String> ppap) {
        return ppap.orElse("???") + "W";
    }

    private List<String> toLines(ForecastSummary forecastSummary, ZonedDateTime now) {
        return forecastSummary.getValues().stream().map(value -> toLine(value, now)).toList();
    }

    private String toLine(RealtimeSummary summary) {
        return toLine("Actu.", //
                summary.getPrecipitationProbability(), //
                summary.getWeatherCode(), //
                summary.getTemperatureApparent());
    }

    private String toLine(ForecastData forecast, ZonedDateTime now) {
        var nextDayMarker = toNextDayMarker(now, forecast);
        var firstCol = toHourRange(forecast) + nextDayMarker;
        return toLine(firstCol, //
                forecast.getPrecipitationProbability(), //
                forecast.getWeatherCode(), //
                forecast.getTemperatureApparent());
    }

    private String toLine(String firstCol, double precipitation, int weatherCode, double temperature) {
        var precipitationProbabilityStr = toString(precipitation) + "%";
        var weatherStr = properties.getProperty("weather_code_" + weatherCode);
        var temperatureStr = toTemperatureStr(Optional.of(temperature));
        return toLineLayoutV2(firstCol, //
                temperatureStr, //
                precipitationProbabilityStr, //
                weatherStr);
    }

    private static String toNextDayMarker(ZonedDateTime now, ForecastData forecastData) {
        var midnight = now //
                .withHour(0) //
                .withMinute(0) //
                .withSecond(0) //
                .withNano(0) //
                .plusDays(1);
        return forecastData.getFrom().isAfter(midnight) ? "'" : "";
    }

    private String toHourRange(ForecastData forecastData) {
        return format(forecastData.getFrom()) + "/" + format(forecastData.getTo());
    }

    private String format(ZonedDateTime dateTime) {
        var hour = Integer.toString(dateTime.getHour());
        return leftPad(hour, 2, "0");
    }

    private String toLineLayoutV1(String col1, String col2, String col3) {
        return rightPad(col1, 11) + " " + leftPad(col2, 3) + " " + leftPad(col3, 5);
    }

    private String toLineLayoutV2(String col1, String col2, String col3, String col4) {
        return rightPad(col1, 6) + " " + leftPad(col2, 3) + " " + leftPad(col3, 4) + " " + rightPad(col4, 5);
    }

}
