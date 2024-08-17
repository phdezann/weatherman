package org.phdezann.weather.core;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import org.apache.hc.core5.net.URIBuilder;
import org.phdezann.weather.cache.TomorrowIOCache;
import org.phdezann.weather.cache.TomorrowIOCache.CacheKey;
import org.phdezann.weather.support.OffTheGridException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class TomorrowIOClient {

    private final AppArgs appArgs;
    private final TomorrowIOCache cache;

    public String getRealtimeContent() {
        return cache.getOrAdd(CacheKey.TOMORROW_IO_REALTIME, this::doGetRealtimeContent);
    }

    private String doGetRealtimeContent() {
        if (appArgs.isFakeWeatherData()) {
            Sleeper.wait(2);
            return "N/A";
        }
        if (isTomorrowIOUnreachable()) {
            throw new OffTheGridException();
        }
        try {
            URI uri = new URIBuilder("https://api.tomorrow.io/v4/weather/realtime") //
                    .addParameter("apikey", appArgs.getTomorrowIoApiKey()) //
                    .addParameter("units", "metric") //
                    .addParameter("location", appArgs.getWeatherLocation()) //
                    .build();
            return sendRequest(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String getForecastContent() {
        return cache.getOrAdd(CacheKey.TOMORROW_IO_FORECAST, this::doGetForecastContent);
    }

    private String doGetForecastContent() {
        if (appArgs.isFakeWeatherData()) {
            Sleeper.wait(2);
            return "N/A";
        }
        if (isTomorrowIOUnreachable()) {
            throw new OffTheGridException();
        }
        try {
            var uri = new URIBuilder("https://api.tomorrow.io/v4/weather/forecast") //
                    .addParameter("apikey", appArgs.getTomorrowIoApiKey()) //
                    .addParameter("units", "metric") //
                    .addParameter("timesteps", "1h") //
                    .addParameter("location", appArgs.getWeatherLocation()) //
                    .build();
            return sendRequest(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private String sendRequest(URI uri) {
        try {
            var request = HttpRequest.newBuilder(uri).GET().build();
            log.info("Sending request to {}", toPrettyString(uri));
            var response = HttpClient //
                    .newBuilder() //
                    .connectTimeout(Duration.ofSeconds(2)) //
                    .build() //
                    .send(request, BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Status code was " + response.statusCode());
            }
            var body = response.body();
            log.info("Tomorrow.io data downloaded");
            return body;
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String toPrettyString(URI uri) {
        try {
            return new URI(uri.getScheme(), //
                    uri.getAuthority(), //
                    uri.getPath(), //
                    null, // Ignore the query part of the input url
                    uri.getFragment()).toString();
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean isTomorrowIOUnreachable() {
        try {
            URI uri = new URIBuilder("https://www.tomorrow.io").build();
            sendRequest(uri);
            return false;
        } catch (Exception ex) {
            return true;
        }
    }
}
