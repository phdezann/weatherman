package org.phdezann.home.console.core;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.phdezann.home.console.core.InfluxDBClient.QueryParams;
import org.phdezann.home.console.core.InfluxDBClient.Record;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class InfluxDBRepository {

    public Optional<DateValue> currentConsumption(ZonedDateTime now, Function<QueryParams, List<Record>> queryRunner) {
        now = toEuropeParis(now);
        var start = now //
                .withHour(6) //
                .withMinute(0);
        var stop = now;
        var records = queryRunner.apply(newQueryParams(start, stop));
        var hour = stop.getHour();
        var minute = stop.getMinute();
        var values = records //
                .stream() //
                .filter(result -> {
                    var time = toEuropeParis(result.getTime());
                    return time.getHour() == hour && time.getMinute() == minute;
                }) //
                .map(r -> new DateTimeValue(r.getTime(), r.getValue())) //
                .toList();
        var dateValues = computeDiff(values, records); //
        if (dateValues.isEmpty()) {
            log.warn("Could not find consumption for '{}'", now);
            return Optional.empty();
        }
        return Optional.of(dateValues.get(0));
    }

    public List<DateValue> historicalConsumption(ZonedDateTime now, int daysInThePast,
            Function<QueryParams, List<Record>> queryRunner) {
        now = toEuropeParis(now);
        var stop = findLastTime(now, 6, 0);
        var start = stop.minusDays(daysInThePast);
        var hour = stop.getHour();
        var minute = stop.getMinute();
        var records = queryRunner.apply(newQueryParams(start, stop));
        var values = records //
                .stream() //
                .filter(r -> {
                    var time = toEuropeParis(r.getTime());
                    return time.getHour() == hour && time.getMinute() == minute;
                }) //
                .map(r -> new DateTimeValue(r.getTime(), r.getValue())) //
                .toList();
        return computeDiff(values);
    }

    public List<DateValue> realtimeConsumption(ZonedDateTime now, int daysInThePast,
            Function<QueryParams, List<Record>> queryRunner) {
        now = toEuropeParis(now);
        var start = now //
                .withHour(6) //
                .withMinute(0) //
                .minusDays(daysInThePast);
        var stop = now //
                .minusDays(1);
        var hour = stop.getHour();
        var minute = stop.getMinute();
        var records = queryRunner.apply(newQueryParams(start, stop));
        var values = records //
                .stream() //
                .filter(r -> {
                    var time = toEuropeParis(r.getTime());
                    return time.getHour() == hour && time.getMinute() == minute;
                }).map(record -> new DateTimeValue(record.getTime(), record.getValue())) //
                .toList();
        return computeDiff(values, records);
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    @ToString
    @Getter
    public static class DateValue {
        private final LocalDate day;
        private final BigDecimal value;
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    @ToString
    @Getter
    public static class DateTimeValue {
        private final ZonedDateTime dateTime;
        private final BigDecimal value;
    }

    private QueryParams newQueryParams(ZonedDateTime start, ZonedDateTime end) {
        var queryParams = new QueryParams(roundedForInfluxDB(start), roundedForInfluxDB(end));
        return includeStart(queryParams);
    }

    private ZonedDateTime roundedForInfluxDB(ZonedDateTime dateTime) {
        return dateTime //
                .withSecond(0) //
                .truncatedTo(ChronoUnit.SECONDS);
    }

    private QueryParams includeStart(QueryParams queryParams) {
        return new QueryParams(queryParams.getStart().minusMinutes(1), queryParams.getStop());
    }

    private List<DateValue> computeDiff(List<DateTimeValue> values) {
        return values //
                .stream() //
                .map(currentValue -> values //
                        .stream() //
                        .filter(v -> v.getDateTime().equals(currentValue.getDateTime().minusDays(1))) //
                        .findFirst() //
                        .map(v -> {
                            var date = v.getDateTime().toLocalDate();
                            var diff = currentValue.getValue().subtract(v.getValue());
                            return new DateValue(date, diff);
                        }))
                .filter(Optional::isPresent) //
                .map(Optional::get) //
                .toList();
    }

    private List<DateValue> computeDiff(List<DateTimeValue> values, List<Record> records) {
        return values //
                .stream() //
                .map(value -> {
                    var startTime = toEuropeParis(value.getDateTime()).withHour(6).withMinute(0);
                    var start = records //
                            .stream() //
                            .filter(r -> r.getTime().isEqual(startTime)) //
                            .findFirst();
                    if (start.isEmpty()) {
                        return Optional.<DateValue> empty();
                    }
                    var day = value.getDateTime().toLocalDate();
                    var diff = value.getValue().subtract(start.get().getValue());
                    return Optional.of(new DateValue(day, diff));
                }) //
                .filter(Optional::isPresent) //
                .map(Optional::get) //
                .toList();
    }

    private ZonedDateTime toEuropeParis(ZonedDateTime dateTime) {
        return dateTime.withZoneSameInstant(ZoneId.of("Europe/Paris"));
    }

    private ZonedDateTime findLastTime(ZonedDateTime dateTime, int hour, int minute) {
        var last = dateTime //
                .withHour(hour) //
                .withMinute(minute);
        return last.isBefore(dateTime) ? last : last.minusDays(1);
    }

}
