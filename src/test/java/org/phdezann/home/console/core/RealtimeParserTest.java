package org.phdezann.home.console.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

class RealtimeParserTest {

    private final RealtimeParser realtimeParser = new RealtimeParser(new AppArgs(), new JsonSerializer());

    @Test
    void parse() throws Exception {
        var in = getClass().getResourceAsStream("/timeline.json");
        if (in == null) {
            throw new IllegalArgumentException();
        }
        var json = IOUtils.toString(in, StandardCharsets.UTF_8);
        var parse = realtimeParser.parse(json);

        assertThat(parse.getZonedDateTime().toString()).isEqualTo("2023-11-30T15:41+01:00[Europe/Paris]");
        assertThat(parse.getPrecipitationProbability()).isEqualTo(42.0);
    }

}
