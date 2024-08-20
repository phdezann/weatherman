package org.phdezann.home.console.core;

import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.phdezann.home.console.bus.MqttTopic;
import org.phdezann.home.console.json.Message;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MqttScreenwriterInboxPublisher extends AbstractMqttPublisher {

    private Optional<Message> lastMessageSent = Optional.empty();

    public MqttScreenwriterInboxPublisher(AppArgs appArgs, JsonSerializer jsonSerializer,
            SensorTouchTracker sensorTouchTracker) {
        super(jsonSerializer, sensorTouchTracker, "tcp://" + appArgs.getMqttHostname(),
                MqttTopic.SCREENWRITER_INBOX_TOPIC.getTopic());
    }

    @Override
    protected void writeMessage(Message message) {
        var hasSameContent = lastMessageSent //
                .filter(m -> hasSameContent(m, message)) //
                .isPresent();
        if (hasSameContent) {
            log.debug("Discarding identical message '{}'", message);
            return;
        }
        super.writeMessage(message);
        this.lastMessageSent = Optional.of(message);
    }

    private boolean hasSameContent(Message message, Message other) {
        return message.getEvent() == other.getEvent() //
                && StringUtils.equals(message.getPayload(), other.getPayload()) //
                && same(message.getLastTouch(), other.getLastTouch());
    }

    private static boolean same(LocalDateTime dateTime, LocalDateTime other) {
        if (dateTime == null && other == null) {
            return true;
        }
        if (dateTime == null || other == null) {
            return false;
        }
        return dateTime.equals(other);
    }

}
