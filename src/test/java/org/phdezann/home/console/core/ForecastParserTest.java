package org.phdezann.home.console.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.phdezann.home.console.core.DateTimeUtils.toEuropeParisTZ;

import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

class ForecastParserTest {

    private final ForecastParser forecastParser = //
            new ForecastParser(new AppArgs(), new JsonSerializer(), new NextHourCalculator());

    @Test
    void build() throws Exception {
        var in = getClass().getResourceAsStream("/forecast.json");
        if (in == null) {
            throw new IllegalArgumentException();
        }
        var json = IOUtils.toString(in, StandardCharsets.UTF_8);

        var now = toEuropeParisTZ("2023-11-30T14:15");
        var summary = forecastParser.build(json, now);
        var values = summary.getValues();

        assertThat(values.get(0).getFrom().toString()).isEqualTo("2023-11-30T14:00+01:00[Europe/Paris]");
        assertThat(values.get(0).getTo().toString()).isEqualTo("2023-11-30T15:00+01:00[Europe/Paris]");
        assertThat(values.get(0).getPrecipitationProbability()).isEqualTo(43.0);

        assertThat(values.get(1).getFrom().toString()).isEqualTo("2023-11-30T15:00+01:00[Europe/Paris]");
        assertThat(values.get(1).getTo().toString()).isEqualTo("2023-11-30T16:00+01:00[Europe/Paris]");
        assertThat(values.get(1).getPrecipitationProbability()).isEqualTo(0.0);

        assertThat(values.get(2).getFrom().toString()).isEqualTo("2023-11-30T18:00+01:00[Europe/Paris]");
        assertThat(values.get(2).getTo().toString()).isEqualTo("2023-11-30T19:00+01:00[Europe/Paris]");
        assertThat(values.get(2).getPrecipitationProbability()).isEqualTo(44.0);

        assertThat(values.get(3).getFrom().toString()).isEqualTo("2023-12-01T06:00+01:00[Europe/Paris]");
        assertThat(values.get(3).getTo().toString()).isEqualTo("2023-12-01T07:00+01:00[Europe/Paris]");
        assertThat(values.get(3).getPrecipitationProbability()).isEqualTo(15.0);

        assertThat(values.get(4).getFrom().toString()).isEqualTo("2023-12-01T12:00+01:00[Europe/Paris]");
        assertThat(values.get(4).getTo().toString()).isEqualTo("2023-12-01T13:00+01:00[Europe/Paris]");
        assertThat(values.get(4).getPrecipitationProbability()).isEqualTo(5.0);
    }

    @Test
    void parse_noValuesAvailable() throws Exception {
        var resourceAsStream = getClass().getResourceAsStream("/forecast.json");
        if (resourceAsStream == null) {
            throw new IllegalArgumentException();
        }
        var json = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);

        var now = toEuropeParisTZ("2023-12-05T13:00");
        var summary = forecastParser.build(json, now);
        var values = summary.getValues();

        assertThat(values).isEmpty();
    }

}
