package org.phdezann.home.console.core;

import static org.phdezann.home.console.bus.MsgEnum.SHOW_MESSAGE;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.phdezann.home.console.bus.MsgEnum;
import org.phdezann.home.console.cache.InfluxDBCache;
import org.phdezann.home.console.cache.InfluxDBCache.CacheKey;
import org.phdezann.home.console.cache.TemperatureCache;
import org.phdezann.home.console.cache.TicCache;
import org.phdezann.home.console.core.ScreenStateTracker.ScreenState;
import org.phdezann.home.console.printer.ConsumptionPrinter;
import org.phdezann.home.console.printer.WeatherPrinter;
import org.phdezann.home.console.support.OffTheGridException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class MessageSender {

    private static final long DURATION_SCREEN_ON_IN_SECS = 30;
    private static final long OVER_CONSUMPTION_THRESHOLD_IN_WATTS = 500;

    private final TomorrowIOClient tomorrowIOClient;
    private final RealtimeParser realtimeParser;
    private final ForecastParser forecastParser;
    private final WeatherPrinter weatherPrinter;
    private final MqttScreenwriterInboxPublisher mqttScreenwriterInboxPublisher;
    private final TicCache ticCache;
    private final TemperatureCache temperatureCache;
    private final InfluxDBCache influxDBCache;
    private final ScreenStateTracker screenStateTracker;
    private final InfluxDBRepository influxDBRepository;
    private final InfluxDBClient influxDBClient;
    private final ConsumptionPrinter consumptionPrinter;

    public void sendMessage() {
        try {
            if (screenStateTracker.getCurrentState() != ScreenState.SLEEPING) {
                var now = ZonedDateTime.now();
                var lastActivity = screenStateTracker.lastActivityDurationInSecs(now);
                var goToSleep = lastActivity //
                        .filter(duration -> duration > DURATION_SCREEN_ON_IN_SECS) //
                        .isPresent();
                if (goToSleep) {
                    mqttScreenwriterInboxPublisher.writeMessage(MsgEnum.CLEAR_SCREEN);
                    screenStateTracker.toSleepingState();
                } else {
                    var text = switch (screenStateTracker.getCurrentState()) {
                    case AWAKE_WEATHER -> createWeatherText(now);
                    case AWAKE_WEATHER_NEXT_6H -> createWeatherNext6HoursText(now);
                    case AWAKE_REALTIME_CONSUMPTION -> createRealtimeConsumptionText(now);
                    case AWAKE_HISTORICAL_CONSUMPTION -> createHistoricalConsumptionText(now);
                    default -> throw new IllegalStateException();
                    };
                    send(text);
                }
            } else {
                var papp = getPapp();
                var overConsumption = papp //
                        .map(Integer::parseInt) //
                        .filter(value -> value > OVER_CONSUMPTION_THRESHOLD_IN_WATTS).isPresent();
                if (overConsumption) {
                    send(weatherPrinter.print(papp));
                } else {
                    mqttScreenwriterInboxPublisher.writeMessage(MsgEnum.CLEAR_SCREEN);
                }
            }
        } catch (Exception ex) {
            if (ex.getCause() instanceof OffTheGridException) {
                send("Hors connexion :)");
                return;
            }
            send(":/");
            throw ex;
        }
    }

    private String createWeatherText(ZonedDateTime now) {
        var realtimeContent = tomorrowIOClient.getRealtimeContent();
        var realtimeSummary = realtimeParser.parse(realtimeContent);
        var forecastContent = tomorrowIOClient.getForecastContent();
        var forecastSummary = forecastParser.build(forecastContent, now);
        return weatherPrinter.print(realtimeSummary, forecastSummary, getTemperature(), getPapp(), now);
    }

    private String createWeatherNext6HoursText(ZonedDateTime now) {
        var forecastContent = tomorrowIOClient.getForecastContent();
        var forecastSummary = forecastParser.buildNext6Hours(forecastContent, now);
        return weatherPrinter.printNext6Hours(forecastSummary, getTemperature(), getPapp(), now);
    }

    private String createHistoricalConsumptionText(ZonedDateTime now) {
        return influxDBCache.getOrAdd(CacheKey.HISTORICAL, () -> {
            var historical = influxDBRepository.historicalConsumption(now, 6, influxDBClient::query);
            return consumptionPrinter.printHistorical(historical);
        });
    }

    private String createRealtimeConsumptionText(ZonedDateTime now) {
        return influxDBCache.getOrAdd(CacheKey.REALTIME, () -> {
            var current = influxDBRepository.currentConsumption(now, influxDBClient::query);
            var realtime = influxDBRepository.realtimeConsumption(now, 6, influxDBClient::query);
            return consumptionPrinter.printRealtime(current, realtime);
        });
    }

    private Optional<Double> getTemperature() {
        return temperatureCache.get(TemperatureCache.CacheKey.TEMPERATURE);
    }

    private Optional<String> getPapp() {
        return ticCache.get(TicCache.CacheKey.PAPP);
    }

    private void send(String msg) {
        msg = changeDelimiter(msg);
        mqttScreenwriterInboxPublisher.writeMessage(SHOW_MESSAGE, msg);
    }

    private String changeDelimiter(String msg) {
        return StringUtils.replace(msg, "\n", "|");
    }

}
