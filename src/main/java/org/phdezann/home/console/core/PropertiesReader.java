package org.phdezann.home.console.core;

import java.io.IOException;
import java.util.Properties;

public class PropertiesReader {

    public Properties read() {
        try (var in = PropertiesReader.class.getResourceAsStream("/app.properties")) {
            Properties prop = new Properties();
            prop.load(in);
            return prop;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
