package org.phdezann.home.console.bus;

import lombok.Getter;

public enum MqttTopic {

    WEATHERMAN_INBOX_TOPIC("weatherman/main/inbox"),
    SCREENWRITER_INBOX_TOPIC("screenwriter/main/inbox"),
    //
    REFRESH_TOPIC("weatherman/main/refresh"),
    TIC_EVENT_TOPIC("tele/tasmota_1C12B4/SENSOR"),
    TEMPERATURE_EVENT_TOPIC("weather/probe_KI8Q49/SENSOR");

    @Getter
    private final String topic;

    MqttTopic(String topic) {
        this.topic = topic;
    }

}
