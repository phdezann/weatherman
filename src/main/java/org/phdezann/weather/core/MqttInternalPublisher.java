package org.phdezann.weather.core;

import org.phdezann.weather.bus.MqttTopic;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MqttInternalPublisher extends AbstractMqttPublisher {

    public MqttInternalPublisher(AppArgs appArgs, JsonSerializer jsonSerializer, SensorTouchTracker sensorTouchTracker) {
        super(jsonSerializer, sensorTouchTracker, "tcp://" + appArgs.getMqttHostname(), MqttTopic.REFRESH_TOPIC.getTopic());
    }

}
