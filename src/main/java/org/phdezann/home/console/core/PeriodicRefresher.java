package org.phdezann.home.console.core;

import org.phdezann.home.console.bus.MsgEnum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class PeriodicRefresher {

    private static final int TIME_BETWEEN_REFRESH_IN_SECS = 60 * 5;

    private final TerminationLock terminationLock;
    private final MqttInternalPublisher mqttInternalPublisher;

    private void startRefreshingPeriodically() {
        while (true) {
            try {
                mqttInternalPublisher.writeMessage(MsgEnum.REFRESH_SCREEN);
            } catch (Exception ex) {
                terminationLock.signalAbnormalTermination();
                throw ex;
            }

            log.info("Waiting {} secs", TIME_BETWEEN_REFRESH_IN_SECS);
            Sleeper.wait(TIME_BETWEEN_REFRESH_IN_SECS);
        }
    }

    public void startRefreshingPeriodicallyAsync() {
        new Thread(this::startRefreshingPeriodically).start();
    }

}
