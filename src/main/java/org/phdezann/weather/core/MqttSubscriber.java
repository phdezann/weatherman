package org.phdezann.weather.core;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.phdezann.weather.bus.MqttTopic;
import org.phdezann.weather.bus.MsgEnum;
import org.phdezann.weather.cache.TemperatureCache;
import org.phdezann.weather.cache.TicCache;
import org.phdezann.weather.cache.TicCache.CacheKey;
import org.phdezann.weather.json.Message;
import org.phdezann.weather.json.TemperatureRoot;
import org.phdezann.weather.json.TicRoot;

import lombok.extern.slf4j.Slf4j;

@Slf4j

public class MqttSubscriber extends AbstractMqttSubscriber {

    private final TerminationLock terminationLock;
    private final JsonSerializer jsonSerializer;
    private final TicCache ticCache;
    private final TemperatureCache temperatureCache;
    private final ScreenStateTracker screenStateTracker;
    private final MessageSender messageSender;
    private final MqttInternalPublisher mqttInternalPublisher;
    private final SensorTouchTracker sensorTouchTracker;

    public MqttSubscriber(AppArgs appArgs, TerminationLock terminationLock, JsonSerializer jsonSerializer,
            TicCache ticCache, TemperatureCache temperatureCache, ScreenStateTracker screenStateTracker,
            MessageSender messageSender, MqttInternalPublisher mqttInternalPublisher,
            SensorTouchTracker sensorTouchTracker) {
        super("tcp://" + appArgs.getMqttHostname(), //
                List.of(MqttTopic.REFRESH_TOPIC.getTopic(), //
                        MqttTopic.TIC_EVENT_TOPIC.getTopic(), //
                        MqttTopic.WEATHERMAN_INBOX_TOPIC.getTopic(), //
                        MqttTopic.TEMPERATURE_EVENT_TOPIC.getTopic()));
        this.terminationLock = terminationLock;
        this.jsonSerializer = jsonSerializer;
        this.ticCache = ticCache;
        this.temperatureCache = temperatureCache;
        this.screenStateTracker = screenStateTracker;
        this.messageSender = messageSender;
        this.mqttInternalPublisher = mqttInternalPublisher;
        this.sensorTouchTracker = sensorTouchTracker;
    }

    @Override
    protected void messageArrived(String topic, MqttMessage message) {
        try {
            processMessage(topic, message);
        } catch (Exception ex) {
            log.error("Got exception", ex);
            terminationLock.signalAbnormalTermination();
            throw ex;
        }
    }

    private void processMessage(String topic, MqttMessage message) {
        var rawMsg = new String(message.getPayload(), StandardCharsets.UTF_8);

        if (isFrom(topic, MqttTopic.WEATHERMAN_INBOX_TOPIC) || isFrom(topic, MqttTopic.REFRESH_TOPIC)) {
            var msg = jsonSerializer.readValue(rawMsg, Message.class);

            if (isFrom(topic, MqttTopic.WEATHERMAN_INBOX_TOPIC)) {
                if (msg.getEvent() == MsgEnum.SENSOR_TOUCHED) {
                    screenStateTracker.sensorTouched();
                    sensorTouchTracker.update(LocalDateTime.parse(msg.getPayload()));
                    sendRefreshScreen();
                    return;
                }
            }

            if (isFrom(topic, MqttTopic.REFRESH_TOPIC) && msg.getEvent() == MsgEnum.REFRESH_SCREEN) {
                messageSender.sendMessage();
                return;
            }
        }

        if (isFrom(topic, MqttTopic.TIC_EVENT_TOPIC)) {
            var tic = jsonSerializer.readValue(rawMsg, TicRoot.class);
            var papp = tic.getTic().getPapp();
            if (StringUtils.isNoneEmpty(papp)) {
                ticCache.put(CacheKey.PAPP, papp);
            }
            sendRefreshScreen();
            return;
        }

        if (isFrom(topic, MqttTopic.TEMPERATURE_EVENT_TOPIC)) {
            var temperature = jsonSerializer.readValue(rawMsg, TemperatureRoot.class);
            temperatureCache.put(TemperatureCache.CacheKey.TEMPERATURE, temperature.getTemperature());
            sendRefreshScreen();
            return;
        }

        throw new IllegalArgumentException("Unknown topic: " + topic);
    }

    private boolean isFrom(String topic, MqttTopic mqttTopic) {
        return StringUtils.equals(topic, mqttTopic.getTopic());
    }

    private void sendRefreshScreen() {
        mqttInternalPublisher.writeMessage(MsgEnum.REFRESH_SCREEN);
    }

}
