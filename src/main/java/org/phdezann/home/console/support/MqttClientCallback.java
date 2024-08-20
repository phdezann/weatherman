package org.phdezann.home.console.support;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

public class MqttClientCallback implements MqttCallback {

    @Override
    public void disconnected(MqttDisconnectResponse disconnectResponse) {
        // nothing to do by default
    }

    @Override
    public void mqttErrorOccurred(MqttException exception) {
        // nothing to do by default
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        // nothing to do by default
    }

    @Override
    public void deliveryComplete(IMqttToken token) {
        // nothing to do by default
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        // nothing to do by default
    }

    @Override
    public void authPacketArrived(int reasonCode, MqttProperties properties) {
        // nothing to do by default
    }

}
