package org.phdezann.home.console;

import org.phdezann.home.console.cache.InfluxDBCache;
import org.phdezann.home.console.core.AppArgs;
import org.phdezann.home.console.core.AsciiProgressBarBuilder;
import org.phdezann.home.console.core.SensorTouchTracker;
import org.phdezann.home.console.printer.ConsumptionPrinter;
import org.phdezann.home.console.core.ForecastParser;
import org.phdezann.home.console.core.InfluxDBClient;
import org.phdezann.home.console.core.InfluxDBRepository;
import org.phdezann.home.console.core.JsonSerializer;
import org.phdezann.home.console.core.MqttInternalPublisher;
import org.phdezann.home.console.core.MqttSubscriber;
import org.phdezann.home.console.core.ScreenStateTracker;
import org.phdezann.home.console.core.TerminationLock;
import org.phdezann.home.console.core.PeriodicRefresher;
import org.phdezann.home.console.core.MqttScreenwriterInboxPublisher;
import org.phdezann.home.console.core.NextHourCalculator;
import org.phdezann.home.console.printer.WeatherPrinter;
import org.phdezann.home.console.core.PropertiesReader;
import org.phdezann.home.console.core.RealtimeParser;
import org.phdezann.home.console.core.MessageSender;
import org.phdezann.home.console.cache.TemperatureCache;
import org.phdezann.home.console.cache.TicCache;
import org.phdezann.home.console.cache.TomorrowIOCache;
import org.phdezann.home.console.core.TomorrowIOClient;

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
