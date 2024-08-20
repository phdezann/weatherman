package org.phdezann.home.console.core;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptionsBuilder;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.phdezann.home.console.bus.MsgEnum;
import org.phdezann.home.console.json.Message;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractMqttPublisher {

    private final JsonSerializer jsonSerializer;
    private final SensorTouchTracker sensorTouchTracker;
    private final String serverUri;
    private final String topic;
    private final MqttClient client;
    private final String username;
    private final String password;

    protected AbstractMqttPublisher(JsonSerializer jsonSerializer, SensorTouchTracker sensorTouchTracker,
            String serverUri, String username, String password, String topic) {
        this.jsonSerializer = jsonSerializer;
        this.sensorTouchTracker = sensorTouchTracker;
        this.serverUri = serverUri;
        this.username = username;
        this.password = password;
        this.topic = topic;
        var randomId = UUID.randomUUID().toString();
        this.client = newClient(randomId);
    }

    protected AbstractMqttPublisher(JsonSerializer jsonSerializer, SensorTouchTracker sensorTouchTracker,
            String serverUri, String topic) {
        this(jsonSerializer, sensorTouchTracker, serverUri, null, null, topic);
    }

    private MqttClient newClient(String randomId) {
        try {
            return new MqttClient(this.serverUri, randomId, null);
        } catch (MqttException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void writeMessage(MsgEnum msgEnum) {
        writeMessage(msgEnum, Optional.empty());
    }

    public void writeMessage(MsgEnum msgEnum, String payload) {
        writeMessage(msgEnum, Optional.of(payload));
    }

    private void writeMessage(MsgEnum msgEnum, Optional<String> payload) {
        var now = LocalDateTime.now();
        var lastTouch = sensorTouchTracker.getLastTouch();
        var message = new Message(msgEnum, payload.orElse(null), lastTouch.orElse(null), now);
        writeMessage(message);
    }

    protected void writeMessage(Message message) {
        var json = jsonSerializer.writeValue(message);
        var bytes = json.getBytes(StandardCharsets.UTF_8);
        writeMessage(new MqttMessage(bytes));
    }

    public void writeMessage(MqttMessage message) {
        try {
            connect();
            this.client.publish(topic, message);
        } catch (Exception ex) {
            log.info("Failed to publish, exception was caught", ex);
        }
    }

    // this method *must* be synchronized, caller can come from any thread
    // and this connect method must be called only once.
    private synchronized void connect() {
        if (!this.client.isConnected()) {
            try {
                var options = new MqttConnectionOptionsBuilder().automaticReconnect(true);
                if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                    options = options //
                            .username(username) //
                            .password(password.getBytes(StandardCharsets.UTF_8));
                }
                this.client.connect(options.build());
            } catch (Exception ex) {
                log.info("Failed to connect, exception was caught", ex);
            }
        }
    }

}
