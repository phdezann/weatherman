package org.phdezann.home.console.core;

import static java.time.LocalDate.parse;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.phdezann.home.console.core.InfluxDBClient.QueryParams;
import org.phdezann.home.console.core.InfluxDBClient.Record;
import org.phdezann.home.console.core.InfluxDBRepository.DateValue;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class InfluxDBRepositoryTest {

    private final InfluxDBRepository influxDBRepository = new InfluxDBRepository();

    @Test
    void historicalConsumption() {
        var records = List.of( //
                newRecord("2007-12-02T06:00:00", "0.9"), //
                newRecord("2007-12-03T06:00:00", "1.7"), //
                newRecord("2007-12-04T06:00:00", "3.1"), //
                newRecord("2007-12-05T06:00:00", "3.2"));

        var now = newEuropeParisDateTime("2007-12-05T10:16:30");

        var expectedQueryParam = new QueryParams( //
                newEuropeParisDateTime("2007-12-02T06:00:00").minusMinutes(1), //
                newEuropeParisDateTime("2007-12-05T06:00:00"));

        var results = influxDBRepository.historicalConsumption(now, 3, queryRunner(expectedQueryParam, records));

        assertThat(results).containsExactly( //
                newValue("2007-12-02", "0.8"), //
                newValue("2007-12-03", "1.4"), //
                newValue("2007-12-04", "0.1"));
    }

    @Test
    void historicalConsumption_at4am() {
        var records = List.of( //
                newRecord("2007-12-01T06:00:00", "0.8"), //
                newRecord("2007-12-02T06:00:00", "0.9"), //
                newRecord("2007-12-03T06:00:00", "1.7"), //
                newRecord("2007-12-04T06:00:00", "3.1"));

        var now = newEuropeParisDateTime("2007-12-05T04:16:30");

        var expectedQueryParam = new QueryParams( //
                newEuropeParisDateTime("2007-12-01T06:00:00").minusMinutes(1), //
                newEuropeParisDateTime("2007-12-04T06:00:00"));

        var results = influxDBRepository.historicalConsumption(now, 3, queryRunner(expectedQueryParam, records));

        assertThat(results).containsExactly( //
                newValue("2007-12-01", "0.1"), //
                newValue("2007-12-02", "0.8"), //
                newValue("2007-12-03", "1.4"));
    }

    @Test
    void realtimeConsumption() {
        var records = List.of( //
                newRecord("2007-12-02T06:00:00", "0.9"), //
                newRecord("2007-12-02T10:16:00", "1.2"), //
                newRecord("2007-12-03T06:00:00", "1.7"), //
                newRecord("2007-12-03T10:16:00", "2.9"), //
                newRecord("2007-12-04T06:00:00", "3.1"), //
                newRecord("2007-12-04T10:16:00", "3.9"));

        var now = newEuropeParisDateTime("2007-12-05T10:16:30");

        var expectedQueryParams = new QueryParams( //
                newEuropeParisDateTime("2007-12-02T06:00:00").minusMinutes(1), //
                newEuropeParisDateTime("2007-12-04T10:16:00"));

        var results = influxDBRepository.realtimeConsumption(now, 3, queryRunner(expectedQueryParams, records));

        assertThat(results).containsExactly( //
                newValue("2007-12-02", "0.3"), //
                newValue("2007-12-03", "1.2"), //
                newValue("2007-12-04", "0.8"));
    }

    @Test
    void currentConsumption() {
        var records = List.of( //
                newRecord("2007-12-05T06:00:00", "1.1"), //
                newRecord("2007-12-05T10:16:00", "1.5"));

        var now = newEuropeParisDateTime("2007-12-05T10:16:30");

        var expectedQueryParams = new QueryParams( //
                newEuropeParisDateTime("2007-12-05T06:00:00").minusMinutes(1), //
                newEuropeParisDateTime("2007-12-05T10:16:00"));

        var result = influxDBRepository.currentConsumption(now, queryRunner(expectedQueryParams, records));

        assertThat(result).isEqualTo(Optional.of(newValue("2007-12-05", "0.4")));
    }

    @Test
    void currentConsumption_missingDayStartValue() {
        var records = List.of( //
                newRecord("2007-12-05T10:16:00", "1.5"));

        var now = newEuropeParisDateTime("2007-12-05T10:16:30");

        var expectedQueryParams = new QueryParams( //
                newEuropeParisDateTime("2007-12-05T06:00:00").minusMinutes(1), //
                newEuropeParisDateTime("2007-12-05T10:16:00"));

        var result = influxDBRepository.currentConsumption(now, queryRunner(expectedQueryParams, records));

        assertThat(result).isEmpty();
    }


    private Function<QueryParams, List<Record>> queryRunner(QueryParams expectedQueryParam, List<Record> records) {
        return queryParams -> {
            assertQueryParams(queryParams, expectedQueryParam);
            checkWithinBounds(queryParams, records);
            log(queryParams, records);
            return records;
        };
    }

    private void checkWithinBounds(QueryParams query, List<Record> records) {
        var notWithinBoundsRecords = records //
                .stream() //
                .filter(r -> r.getTime().isBefore(query.getStart()) || r.getTime().isAfter(query.getStop())) //
                .toList();

        var recordsAsStr = notWithinBoundsRecords //
                .stream() //
                .map(Record::toString) //
                .collect(Collectors.joining("\n"));
        var msg = String.format("Detected records that are not within bounds of query, fix this test!\n" //
                + "Query start: '%s'\n" //
                + "Query stop: '%s'\n" //
                + "Records: \n'%s'", query.getStart(), query.getStop(), recordsAsStr);

        assertThat(notWithinBoundsRecords) //
                .withFailMessage(msg) //
                .isEmpty();
    }

    private void log(QueryParams query, List<Record> record) {
        var msg = record //
                .stream() //
                .map(Record::toString) //
                .collect(Collectors.joining("\n"));
        log.debug("Running query and returning records.\n" //
                + "Query start: '{}'\n" //
                + "Query stop: '{}'\n" //
                + "Records: \n'{}'", query.getStart(), query.getStop(), msg);
    }

    private Record newRecord(String dateTime, String value) {
        // to UTC to imitate what InfluxDBClient would return
        var time = newEuropeParisDateTime(dateTime) //
                .withZoneSameInstant(ZoneId.of("Z"));
        return new Record(new BigDecimal(value), time);
    }

    private ZonedDateTime newEuropeParisDateTime(String value) {
        return ZonedDateTime.parse(value + "+01:00[Europe/Paris]");
    }

    private DateValue newValue(String date, String value) {
        return new DateValue(parse(date), new BigDecimal(value));
    }

    private static void assertQueryParams(QueryParams queryParams, QueryParams expectedQueryParams) {
        assertThat(queryParams).isEqualTo(expectedQueryParams);
    }

}
