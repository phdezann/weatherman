package org.phdezann.home.console.core;

import static java.util.Optional.ofNullable;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.query.FluxRecord;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
public class InfluxDBClient {

    @RequiredArgsConstructor
    @EqualsAndHashCode
    @Getter
    @ToString
    public static class QueryParams {
        private final ZonedDateTime start;
        private final ZonedDateTime stop;
    }

    @RequiredArgsConstructor
    @Getter
    @ToString
    public static class Record {
        private final BigDecimal value;
        private final ZonedDateTime time;
    }

    private final AppArgs appArgs;

    public List<Record> query(QueryParams queryParams) {
        try (com.influxdb.client.InfluxDBClient influxDBClient = createInfluxDBClient()) {
            var queryApi = influxDBClient.getQueryApi();
            var query = buildQuery(queryParams);
            var results = queryApi.query(query);
            if (results.isEmpty()) {
                return new ArrayList<>();
            }
            return results //
                    .get(0) //
                    .getRecords() //
                    .stream() //
                    .map(this::map) //
                    .toList();
        }
    }

    private com.influxdb.client.InfluxDBClient createInfluxDBClient() {
        var url = String.format("http://%s:%s", appArgs.getInfluxdbHostname(), appArgs.getInfluxdbPort());
        return InfluxDBClientFactory //
                .create(url, //
                        appArgs.getInfluxdbToken().toCharArray(), //
                        appArgs.getInfluxdbOrg(), //
                        appArgs.getInfluxdbBucket());
    }

    private String buildQuery(QueryParams params) {

        return String.format("""
                from(bucket: \"linky\")
                  |> range(start: %s, stop: %s)
                  |> filter(fn: (r) => r[\"owner\"] == \"1C12B4\")
                  |> filter(fn: (r) => r[\"_field\"] == \"BASE\")
                  |> filter(fn: (r) => r[\"_measurement\"] == \"linky\")
                  |> aggregateWindow(every: 1m, fn: max)
                  |> filter(fn: (r) => exists r._value)""", //
                formatDateTimeISO(params.getStart()), formatDateTimeISO(params.getStop()));
    }

    private Record map(FluxRecord record) {
        var rawTime = ofNullable(record.getValueByKey("_time")).orElseThrow().toString();
        var rawValue = ofNullable(record.getValueByKey("_value")).orElseThrow().toString();

        var to = ZonedDateTime.parse(rawTime);
        var value = toBigDecimal(rawValue);

        return new Record(value, to);
    }

    private String formatDateTimeISO(ZonedDateTime dateTime) {
        var withZone = dateTime.format(DateTimeFormatter.ISO_DATE_TIME);
        return StringUtils.substringBefore(withZone, "[");
    }

    private BigDecimal toBigDecimal(String value) {
        var pattern = Pattern.compile("(\\d+\\.\\d{0,3}).*");
        var matcher = pattern.matcher(value);
        if (!matcher.matches()) {
            throw new RuntimeException();
        }
        return new BigDecimal(matcher.group(1));
    }

}
