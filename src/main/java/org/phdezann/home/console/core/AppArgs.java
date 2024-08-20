package org.phdezann.home.console.core;

import java.io.File;

import com.beust.jcommander.Parameter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppArgs {
    @Parameter(names = "--tomorrow-io-api-key", required = true)
    private String tomorrowIoApiKey;
    @Parameter(names = "--mqtt-hostname", required = true)
    private String mqttHostname;
    @Parameter(names = "--influxdb-hostname", required = true)
    private File influxdbHostname;
    @Parameter(names = "--influxdb-port")
    private int influxdbPort = 8086;
    @Parameter(names = "--influxdb-token", required = true)
    private String influxdbToken;
    @Parameter(names = "--influxdb-org", required = true)
    private String influxdbOrg;
    @Parameter(names = "--influxdb-bucket", required = true)
    private String influxdbBucket;
    @Parameter(names = "--weather-location", required = true)
    private String weatherLocation;
    @Parameter(names = "--static-weather-data")
    private boolean fakeWeatherData;
}
