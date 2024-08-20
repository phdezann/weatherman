package org.phdezann.home.console.core;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScreenStateTracker {

    enum ScreenState {
        AWAKE_WEATHER,
        AWAKE_WEATHER_NEXT_6H,
        AWAKE_REALTIME_CONSUMPTION,
        AWAKE_HISTORICAL_CONSUMPTION,
        SLEEPING
    }

    @Getter
    private ScreenState currentState = ScreenState.AWAKE_WEATHER;
    private Optional<ZonedDateTime> lastStateChange = Optional.of(ZonedDateTime.now());

    public void sensorTouched() {
        if (this.currentState == ScreenState.SLEEPING) {
            setNewState(ScreenState.AWAKE_WEATHER);
        } else {
            setNewState(nextAwakeState(this.currentState));
        }
    }

    private ScreenState nextAwakeState(ScreenState awakeState) {
        return switch (awakeState) {
        case AWAKE_WEATHER -> ScreenState.AWAKE_WEATHER_NEXT_6H;
        case AWAKE_WEATHER_NEXT_6H -> ScreenState.AWAKE_REALTIME_CONSUMPTION;
        case AWAKE_REALTIME_CONSUMPTION -> ScreenState.AWAKE_HISTORICAL_CONSUMPTION;
        case AWAKE_HISTORICAL_CONSUMPTION -> ScreenState.AWAKE_WEATHER;
        default -> throw new IllegalArgumentException();
        };
    }

    public void toSleepingState() {
        setNewState(ScreenState.SLEEPING);
    }

    private void setNewState(ScreenState state) {
        this.currentState = state;
        var now = ZonedDateTime.now();
        this.lastStateChange = Optional.of(now);
        log.info("Last state change updated: '{}'", now);
    }

    public Optional<Long> lastActivityDurationInSecs(ZonedDateTime now) {
        return lastStateChange.map(value -> ChronoUnit.SECONDS.between(value, now));
    }

}
