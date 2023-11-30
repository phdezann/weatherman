package org.phdezann.weather;

import org.phdezann.weather.cache.InfluxDBCache;
import org.phdezann.weather.core.AppArgs;
import org.phdezann.weather.core.AsciiProgressBarBuilder;
import org.phdezann.weather.core.SensorTouchTracker;
import org.phdezann.weather.printer.ConsumptionPrinter;
import org.phdezann.weather.core.ForecastParser;
import org.phdezann.weather.core.InfluxDBClient;
import org.phdezann.weather.core.InfluxDBRepository;
import org.phdezann.weather.core.JsonSerializer;
import org.phdezann.weather.core.MqttInternalPublisher;
import org.phdezann.weather.core.MqttSubscriber;
import org.phdezann.weather.core.ScreenStateTracker;
import org.phdezann.weather.core.TerminationLock;
import org.phdezann.weather.core.PeriodicRefresher;
import org.phdezann.weather.core.MqttScreenwriterInboxPublisher;
import org.phdezann.weather.core.NextHourCalculator;
import org.phdezann.weather.printer.WeatherPrinter;
import org.phdezann.weather.core.PropertiesReader;
import org.phdezann.weather.core.RealtimeParser;
import org.phdezann.weather.core.MessageSender;
import org.phdezann.weather.cache.TemperatureCache;
import org.phdezann.weather.cache.TicCache;
import org.phdezann.weather.cache.TomorrowIOCache;
import org.phdezann.weather.core.TomorrowIOClient;

import com.beust.jcommander.JCommander;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {

    public static void main(String[] args) {
        AppArgs appArgs = new AppArgs();
        JCommander.newBuilder().addObject(appArgs).build().parse(args);

        var propertiesReader = new PropertiesReader();
        var properties = propertiesReader.read();

        var lock = new TerminationLock();
        var tomorrowIOCache = new TomorrowIOCache();
        var ticCache = new TicCache();
        var temperatureCache = new TemperatureCache();
        var influxDBCache = new InfluxDBCache();
        var tomorrowIOClient = new TomorrowIOClient(appArgs, tomorrowIOCache);
        var jsonSerializer = new JsonSerializer();
        var nextHourCalculator = new NextHourCalculator();
        var realtimeParser = new RealtimeParser(appArgs, jsonSerializer);
        var forecastParser = new ForecastParser(appArgs, jsonSerializer, nextHourCalculator);
        var asciiProgressBarBuilder = new AsciiProgressBarBuilder();
        var weatherPrinter = new WeatherPrinter(properties, asciiProgressBarBuilder);
        var touchTracker = new SensorTouchTracker();
        var mqttScreenwriterInboxPublisher = new MqttScreenwriterInboxPublisher(appArgs, jsonSerializer, touchTracker);
        var screenStateSwitcher = new ScreenStateTracker();
        var mqttInternalCmdPublisher = new MqttInternalPublisher(appArgs, jsonSerializer, touchTracker);
        var influxDBClient = new InfluxDBClient(appArgs);
        var influxDBRepository = new InfluxDBRepository();
        var consumptionPrinter = new ConsumptionPrinter();
        var periodicRefresher = new PeriodicRefresher(lock, mqttInternalCmdPublisher);

        var messageSender = new MessageSender(tomorrowIOClient, realtimeParser, forecastParser, weatherPrinter,
                mqttScreenwriterInboxPublisher, ticCache, temperatureCache, influxDBCache, screenStateSwitcher,
                influxDBRepository, influxDBClient, consumptionPrinter);
        var mqttSubscriber = new MqttSubscriber(appArgs, lock, jsonSerializer, ticCache, temperatureCache,
                screenStateSwitcher, messageSender, mqttInternalCmdPublisher, touchTracker);

        mqttSubscriber.startReadingMessagesAsync();
        periodicRefresher.startRefreshingPeriodicallyAsync();

        lock.waitForAbnormalTermination();
        System.exit(1);
    }

}
