package org.phdezann.home.console.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NextHourCalculatorTest {

    private final NextHourCalculator nextHourCalculator = new NextHourCalculator();

    @Test
    void getNext_sameDay() {
        var now = DateTimeUtils.toEuropeParisTZ("2023-11-30T11:00");

        assertThat(nextHourCalculator.getNext(now, 12).toString()).isEqualTo("2023-11-30T12:00+01:00[Europe/Paris]");
    }

    @Test
    void getNext_nextDay() {
        var now = DateTimeUtils.toEuropeParisTZ("2023-11-30T19:00");

        assertThat(nextHourCalculator.getNext(now, 12).toString()).isEqualTo("2023-12-01T12:00+01:00[Europe/Paris]");
    }

    @Test
    void getNext_nextDayExactly24Hours() {
        var now = DateTimeUtils.toEuropeParisTZ("2023-11-30T19:00");

        assertThat(nextHourCalculator.getNext(now, 19).toString()).isEqualTo("2023-12-01T19:00+01:00[Europe/Paris]");
    }

}
