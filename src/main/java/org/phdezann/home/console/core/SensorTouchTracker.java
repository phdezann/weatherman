package org.phdezann.home.console.core;

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class SensorTouchTracker {

    private Optional<LocalDateTime> lastTouch = Optional.empty();

    public void update(LocalDateTime value) {
        this.lastTouch = Optional.of(value);
        log.info("Last sensor touch updated: {}", value);
    }

}
