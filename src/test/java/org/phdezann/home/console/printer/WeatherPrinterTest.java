package org.phdezann.home.console.printer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.phdezann.home.console.core.DateTimeUtils.toEuropeParisTZ;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.phdezann.home.console.core.AsciiProgressBarBuilder;
import org.phdezann.home.console.core.ForecastData;
import org.phdezann.home.console.model.ForecastSummary;
import org.phdezann.home.console.model.RealtimeSummary;

class WeatherPrinterTest {

    @Test
    void print() {
        var now = toEuropeParisTZ("2023-11-30T15:41");
        var realtimeSummary = new RealtimeSummary(now, 42.0, 15.0, 1100);
        var forecastSummary = new ForecastSummary(List.of( //
                newForecast("2023-11-30T09:00", "2023-11-30T10:00", 41.0, 14.0, 1100), //
                newForecast("2023-11-30T14:00", "2023-11-30T15:00", 43.0, 15.0, 1100), //
                newForecast("2023-11-30T18:00", "2023-11-30T19:00", 44.0, -0.14, 1101)));

        var properties = new Properties();
        properties.put("weather_code_1100", "Ensoleillé");
        properties.put("weather_code_1101", "Nuageux");
        WeatherPrinter weatherPrinter = new WeatherPrinter(properties, new AsciiProgressBarBuilder());
        var print = weatherPrinter.print(realtimeSummary, forecastSummary, Optional.of(19.1), Optional.of("1220"), now);

        assertThat(print).isEqualTo("" //
                + "30/11 15:41 19° 1220W\n" //
                + "Actu.  15°  42% Enso.\n" //
                + "09/10  14°  41% Enso.\n" //
                + "14/15  15°  43% Enso.\n" //
                + "18/19   0°  44% Nuag.");
    }

    @Test
    void print_ppap() {
        WeatherPrinter weatherPrinter = new WeatherPrinter(new Properties(), new AsciiProgressBarBuilder());
        var print = weatherPrinter.print(Optional.of("1220"));
        assertThat(print).isEqualTo("                1220W\n" //
                                    + "[===================]\n" //
                                    + "[=··················]\n" //
                                    + "[···················]\n" //
                                    + "[···················]\n" //
                                    + "[···················]\n" //
                                    + "[···················]");
    }

    private ForecastData newForecast(String from, //
            String to, //
            double precipitationProbability, //
            double temperatureApparent, //
            int weatherCode) {
        return new ForecastData(toEuropeParisTZ(from), toEuropeParisTZ(to), precipitationProbability,
                temperatureApparent, weatherCode);
    }

}
